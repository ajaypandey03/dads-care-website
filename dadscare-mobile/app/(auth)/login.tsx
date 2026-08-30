import { useAuth } from "@/context/AuthContext";
import { zodResolver } from "@hookform/resolvers/zod";
import { Redirect } from "expo-router";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { ActivityIndicator, Text, TextInput, TouchableOpacity, View } from "react-native";
import { z } from "zod";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});
type FormValues = z.infer<typeof schema>;

export default function LoginScreen() {
  const { user, login } = useAuth();
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  if (user) {
    return <Redirect href="/(app)" />;
  }

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    try {
      await login(values.email, values.password);
    } catch (error: any) {
      // error.response means the server actually answered (wrong credentials, a
      // validation error, etc.) — anything else (no response at all) is a real
      // connectivity problem, and telling the operator "invalid password" for that is
      // actively misleading (found via a real CORS-blocked test: the request never left
      // the device, yet the old code showed "Invalid email or password" regardless).
      if (error?.response) {
        setServerError("Invalid email or password");
      } else {
        setServerError("Couldn't reach the server. Check your connection and try again.");
      }
    }
  };

  return (
    <View className="flex-1 justify-center bg-white px-6">
      <Text className="text-3xl font-bold text-brand mb-2">Dad&apos;s Care</Text>
      <Text className="text-base text-gray-500 mb-8">Sign in to manage your godowns</Text>

      <Text className="text-sm font-medium text-gray-700 mb-1">Email</Text>
      <Controller
        control={control}
        name="email"
        render={({ field: { onChange, onBlur, value } }) => (
          <TextInput
            className="border border-gray-300 rounded-lg px-4 py-3 mb-1 text-base"
            placeholder="you@example.com"
            autoCapitalize="none"
            keyboardType="email-address"
            onBlur={onBlur}
            onChangeText={onChange}
            value={value ?? ""}
          />
        )}
      />
      {errors.email && <Text className="text-red-600 text-sm mb-2">{errors.email.message}</Text>}

      <Text className="text-sm font-medium text-gray-700 mb-1 mt-3">Password</Text>
      <Controller
        control={control}
        name="password"
        render={({ field: { onChange, onBlur, value } }) => (
          <TextInput
            className="border border-gray-300 rounded-lg px-4 py-3 mb-1 text-base"
            placeholder="••••••••"
            secureTextEntry
            onBlur={onBlur}
            onChangeText={onChange}
            value={value ?? ""}
          />
        )}
      />
      {errors.password && <Text className="text-red-600 text-sm mb-2">{errors.password.message}</Text>}

      {serverError && <Text className="text-red-600 text-sm mt-2 text-center">{serverError}</Text>}

      <TouchableOpacity
        className="bg-brand rounded-lg py-4 mt-6 items-center"
        disabled={isSubmitting}
        onPress={handleSubmit(onSubmit)}
      >
        {isSubmitting ? <ActivityIndicator color="white" /> : <Text className="text-white font-semibold text-base">Sign in</Text>}
      </TouchableOpacity>
    </View>
  );
}
