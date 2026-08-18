import { listProductMasters, listTransporterMasters } from "@/api/mastersApi";
import { submitUnlockRequest } from "@/api/unlockApi";
import { PickerField } from "@/components/PickerField";
import { CreateUnlockRequestPayload } from "@/types/forms";
import { queueSubmission } from "@/utils/pendingSubmissionQueue";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQuery } from "@tanstack/react-query";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useState } from "react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import { z } from "zod";

const MAX_CUSTOM_FIELDS = 10;

// Quantities/labor count are typed as `number` on the schema, but TextInput only ever
// gives us strings — each numeric field below converts in its own onChangeText rather
// than via z.coerce, which otherwise produces a resolver type mismatch against
// react-hook-form's inferred FormValues (a known zod v4 + @hookform/resolvers wrinkle).
const schema = z.object({
  stockLines: z.array(
    z.object({ productMasterId: z.number({ message: "Pick a product" }), quantity: z.number().positive() }),
  ),
  truckEntries: z.array(
    z.object({
      source: z.string().min(1, "Required"),
      productMasterId: z.number({ message: "Pick a product" }),
      vehicleNo: z.string().min(1, "Required"),
      transporterMasterId: z.number({ message: "Pick a transporter" }),
      quantity: z.number().positive(),
    }),
  ),
  laborCount: z.number().int().nonnegative().optional(),
  remarks: z.string().max(200).optional(),
  customFields: z.array(z.object({ heading: z.string().min(1), value: z.string().min(1) })).max(MAX_CUSTOM_FIELDS),
});
type FormValues = z.infer<typeof schema>;

/** Converts a TextInput's string value to the number|undefined a numeric form field expects. */
function toNumberOrUndefined(text: string): number | undefined {
  if (text.trim() === "") return undefined;
  const parsed = Number(text);
  return Number.isNaN(parsed) ? undefined : parsed;
}

export default function GodownFormScreen() {
  const { deviceId, mode } = useLocalSearchParams<{ deviceId: string; mode: "open" | "close" }>();
  const router = useRouter();
  const [submitState, setSubmitState] = useState<"idle" | "submitting" | "queued">("idle");

  const { data: products } = useQuery({ queryKey: ["product-masters"], queryFn: listProductMasters });
  const { data: transporters } = useQuery({ queryKey: ["transporter-masters"], queryFn: listTransporterMasters });

  const { control, handleSubmit } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { stockLines: [], truckEntries: [], customFields: [] },
  });
  const stockLines = useFieldArray({ control, name: "stockLines" });
  const truckEntries = useFieldArray({ control, name: "truckEntries" });
  const customFields = useFieldArray({ control, name: "customFields" });

  const productOptions = (products ?? []).map((p) => ({ id: p.id, label: `${p.name} (${p.unit})` }));
  const transporterOptions = (transporters ?? []).map((t) => ({ id: t.id, label: t.name }));

  const onSubmit = async (values: FormValues) => {
    const payload: CreateUnlockRequestPayload = {
      commandType: mode === "open" ? "UNLOCK" : "LOCK",
      stockLines: values.stockLines,
      truckEntries: values.truckEntries,
      laborCount: values.laborCount,
      remarks: values.remarks,
      customFields: values.customFields,
    };

    setSubmitState("submitting");
    try {
      const result = await submitUnlockRequest(Number(deviceId), payload);
      setSubmitState("idle");
      if (result.status === "RELAYED") {
        Alert.alert("Sent", "The command was sent to the lock.", [{ text: "OK", onPress: () => router.back() }]);
      } else {
        // status === "FAILED": Velosyss rejected/couldn't reach the device — a real
        // failure, not a network problem, so this is NOT queued for retry.
        Alert.alert(
          "Could not reach the lock",
          "Your form was saved, but the lock command failed. Please try again or contact support.",
          [{ text: "OK", onPress: () => router.back() }],
        );
      }
    } catch (error: any) {
      if (error?.response) {
        // A real server error (validation, auth, etc.) — not a connectivity problem, don't queue it.
        setSubmitState("idle");
        Alert.alert("Couldn't submit", error.response.data?.error ?? "Please check the form and try again.");
        return;
      }
      // No response at all — a network error. Queue for retry on reconnect.
      await queueSubmission(Number(deviceId), payload);
      setSubmitState("queued");
      Alert.alert(
        "Saved — will submit when back online",
        "You're offline. This form is saved and will be submitted automatically, and the lock will only open once that succeeds.",
        [{ text: "OK", onPress: () => router.back() }],
      );
    }
  };

  return (
    <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} className="flex-1 bg-gray-50">
      <ScrollView contentContainerClassName="p-4" keyboardShouldPersistTaps="handled">
        <Text className="text-xl font-bold text-gray-900 mb-1">{mode === "open" ? "Opening Form" : "Closing Form"}</Text>
        <Text className="text-sm text-gray-500 mb-6">
          {mode === "open"
            ? "Complete this before the shutter is unlocked."
            : "Complete this before the shutter is locked."}
        </Text>

        <Section
          title="Inventory"
          onAdd={() => stockLines.append({ productMasterId: undefined as any, quantity: undefined as any })}
        >
          {stockLines.fields.map((field, index) => (
            <View key={field.id} className="flex-row items-end mb-2" style={{ gap: 8 }}>
              <View className="flex-1">
                <Controller
                  control={control}
                  name={`stockLines.${index}.productMasterId`}
                  render={({ field: { onChange, value } }) => (
                    <PickerField label="Product" options={productOptions} value={value} onChange={onChange} />
                  )}
                />
              </View>
              <View style={{ width: 90 }}>
                <Controller
                  control={control}
                  name={`stockLines.${index}.quantity`}
                  render={({ field: { onChange, value } }) => (
                    <TextInput
                      className="border border-gray-300 rounded-lg px-3 py-3"
                      placeholder="Qty"
                      keyboardType="numeric"
                      value={value != null ? String(value) : ""}
                      onChangeText={(text) => onChange(toNumberOrUndefined(text))}
                    />
                  )}
                />
              </View>
              <RemoveButton onPress={() => stockLines.remove(index)} />
            </View>
          ))}
        </Section>

        <Section
          title="Trucks"
          onAdd={() =>
            truckEntries.append({
              source: "",
              productMasterId: undefined as any,
              vehicleNo: "",
              transporterMasterId: undefined as any,
              quantity: undefined as any,
            })
          }
        >
          {truckEntries.fields.map((field, index) => (
            <View key={field.id} className="border border-gray-200 rounded-lg p-3 mb-3">
              <Controller
                control={control}
                name={`truckEntries.${index}.source`}
                render={({ field: { onChange, value } }) => (
                  <TextInput
                    className="border border-gray-300 rounded-lg px-3 py-3 mb-2"
                    placeholder="Source"
                    value={value}
                    onChangeText={onChange}
                  />
                )}
              />
              <Controller
                control={control}
                name={`truckEntries.${index}.productMasterId`}
                render={({ field: { onChange, value } }) => (
                  <PickerField label="Product" options={productOptions} value={value} onChange={onChange} />
                )}
              />
              <Controller
                control={control}
                name={`truckEntries.${index}.vehicleNo`}
                render={({ field: { onChange, value } }) => (
                  <TextInput
                    className="border border-gray-300 rounded-lg px-3 py-3 mb-2"
                    placeholder="Vehicle No."
                    autoCapitalize="characters"
                    value={value}
                    onChangeText={onChange}
                  />
                )}
              />
              <Controller
                control={control}
                name={`truckEntries.${index}.transporterMasterId`}
                render={({ field: { onChange, value } }) => (
                  <PickerField label="Transporter" options={transporterOptions} value={value} onChange={onChange} />
                )}
              />
              <Controller
                control={control}
                name={`truckEntries.${index}.quantity`}
                render={({ field: { onChange, value } }) => (
                  <TextInput
                    className="border border-gray-300 rounded-lg px-3 py-3 mb-2"
                    placeholder="Quantity"
                    keyboardType="numeric"
                    value={value != null ? String(value) : ""}
                    onChangeText={(text) => onChange(toNumberOrUndefined(text))}
                  />
                )}
              />
              <RemoveButton label="Remove truck" onPress={() => truckEntries.remove(index)} />
            </View>
          ))}
        </Section>

        <Text className="text-base font-semibold text-gray-900 mt-4 mb-2">Labor &amp; Remarks</Text>
        <Controller
          control={control}
          name="laborCount"
          render={({ field: { onChange, value } }) => (
            <TextInput
              className="border border-gray-300 rounded-lg px-3 py-3 mb-2"
              placeholder="Labor count"
              keyboardType="numeric"
              value={value != null ? String(value) : ""}
              onChangeText={(text) => onChange(toNumberOrUndefined(text))}
            />
          )}
        />
        <Controller
          control={control}
          name="remarks"
          render={({ field: { onChange, value } }) => (
            <TextInput
              className="border border-gray-300 rounded-lg px-3 py-3 mb-4"
              placeholder="Remarks"
              multiline
              value={value}
              onChangeText={onChange}
            />
          )}
        />

        <Section
          title="Custom fields"
          onAdd={
            customFields.fields.length < MAX_CUSTOM_FIELDS
              ? () => customFields.append({ heading: "", value: "" })
              : undefined
          }
        >
          {customFields.fields.map((field, index) => (
            <View key={field.id} className="flex-row items-center mb-2" style={{ gap: 8 }}>
              <Controller
                control={control}
                name={`customFields.${index}.heading`}
                render={({ field: { onChange, value } }) => (
                  <TextInput
                    className="flex-1 border border-gray-300 rounded-lg px-3 py-3"
                    placeholder="Heading"
                    value={value}
                    onChangeText={onChange}
                  />
                )}
              />
              <Controller
                control={control}
                name={`customFields.${index}.value`}
                render={({ field: { onChange, value } }) => (
                  <TextInput
                    className="flex-1 border border-gray-300 rounded-lg px-3 py-3"
                    placeholder="Value"
                    value={value}
                    onChangeText={onChange}
                  />
                )}
              />
              <RemoveButton onPress={() => customFields.remove(index)} />
            </View>
          ))}
        </Section>

        <TouchableOpacity
          className="bg-brand rounded-lg py-4 mt-6 mb-12 items-center"
          disabled={submitState === "submitting"}
          onPress={handleSubmit(onSubmit)}
        >
          {submitState === "submitting" ? (
            <ActivityIndicator color="white" />
          ) : (
            <Text className="text-white font-semibold text-base">
              {mode === "open" ? "Submit & Unlock" : "Submit & Lock"}
            </Text>
          )}
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

function Section({
  title,
  onAdd,
  children,
}: {
  title: string;
  onAdd?: () => void;
  children: React.ReactNode;
}) {
  return (
    <View className="mb-4">
      <View className="flex-row items-center justify-between mb-2">
        <Text className="text-base font-semibold text-gray-900">{title}</Text>
        {onAdd && (
          <TouchableOpacity onPress={onAdd}>
            <Text className="text-brand font-medium">+ Add</Text>
          </TouchableOpacity>
        )}
      </View>
      {children}
    </View>
  );
}

function RemoveButton({ onPress, label = "Remove" }: { onPress: () => void; label?: string }) {
  return (
    <TouchableOpacity onPress={onPress} className="py-2">
      <Text className="text-red-600 text-sm">{label}</Text>
    </TouchableOpacity>
  );
}
