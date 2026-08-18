import { useState } from "react";
import { FlatList, Modal, Text, TouchableOpacity, View } from "react-native";

export interface PickerOption {
  id: number;
  label: string;
}

interface PickerFieldProps {
  label: string;
  options: PickerOption[];
  value: number | undefined;
  onChange: (id: number) => void;
  error?: string;
}

/** A minimal modal-based select — React Native has no native <select>, and this avoids pulling in another dependency. */
export function PickerField({ label, options, value, onChange, error }: PickerFieldProps) {
  const [open, setOpen] = useState(false);
  const selected = options.find((o) => o.id === value);

  return (
    <View className="mb-2">
      <TouchableOpacity
        className="border border-gray-300 rounded-lg px-3 py-3"
        onPress={() => setOpen(true)}
      >
        <Text className={selected ? "text-gray-900" : "text-gray-400"}>{selected ? selected.label : label}</Text>
      </TouchableOpacity>
      {error && <Text className="text-red-600 text-xs mt-1">{error}</Text>}

      <Modal visible={open} animationType="slide" transparent onRequestClose={() => setOpen(false)}>
        <TouchableOpacity className="flex-1 bg-black/40 justify-end" activeOpacity={1} onPress={() => setOpen(false)}>
          <View className="bg-white rounded-t-2xl max-h-96">
            <Text className="text-base font-semibold px-4 pt-4 pb-2">{label}</Text>
            <FlatList
              data={options}
              keyExtractor={(o) => String(o.id)}
              renderItem={({ item }) => (
                <TouchableOpacity
                  className="px-4 py-3 border-t border-gray-100"
                  onPress={() => {
                    onChange(item.id);
                    setOpen(false);
                  }}
                >
                  <Text className="text-base text-gray-900">{item.label}</Text>
                </TouchableOpacity>
              )}
              ListEmptyComponent={<Text className="px-4 py-6 text-center text-gray-400">Nothing set up yet</Text>}
            />
          </View>
        </TouchableOpacity>
      </Modal>
    </View>
  );
}
