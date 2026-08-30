import { listShutterUnits } from "@/api/sitesApi";
import { useQuery } from "@tanstack/react-query";
import { Link, useLocalSearchParams } from "expo-router";
import { ActivityIndicator, FlatList, Text, TouchableOpacity, View } from "react-native";

export default function ShutterUnitsScreen() {
  const { siteId } = useLocalSearchParams<{ siteId: string }>();
  const {
    data: shutterUnits,
    isLoading,
    isError,
    refetch,
    isRefetching,
  } = useQuery({
    queryKey: ["shutter-units", siteId],
    queryFn: () => listShutterUnits(Number(siteId)),
  });

  if (isLoading) {
    return <ActivityIndicator className="mt-8" size="large" color="#1E40AF" />;
  }
  if (isError) {
    return <Text className="text-center text-red-600 mt-8">Couldn&apos;t load shutters. Pull down to retry.</Text>;
  }

  return (
    <FlatList
      className="bg-gray-50"
      data={shutterUnits}
      keyExtractor={(unit) => String(unit.id)}
      refreshing={isRefetching}
      onRefresh={refetch}
      contentContainerClassName="p-4"
      ItemSeparatorComponent={() => <View className="h-3" />}
      renderItem={({ item: unit }) => {
        const device = unit.device;
        return (
          <View className="bg-white rounded-xl p-4 border border-gray-200">
            <View className="flex-row items-center justify-between">
              <Text className="text-lg font-semibold text-gray-900">{unit.label}</Text>
              {device && (
                <View className={`px-2 py-1 rounded-full ${device.online ? "bg-green-100" : "bg-gray-200"}`}>
                  <Text className={`text-xs font-medium ${device.online ? "text-green-700" : "text-gray-500"}`}>
                    {device.online ? "Online" : "Offline"}
                  </Text>
                </View>
              )}
            </View>
            {device?.lastBatteryPct != null && (
              <Text className="text-sm text-gray-500 mt-1">Battery: {device.lastBatteryPct}%</Text>
            )}

            {device ? (
              <>
                <Link
                  href={{ pathname: "/(app)/devices/[deviceId]/[mode]", params: { deviceId: device.id, mode: "open" } }}
                  asChild
                >
                  <TouchableOpacity className="bg-brand rounded-lg py-3 items-center mt-4">
                    <Text className="text-white font-semibold">Open Shutter</Text>
                  </TouchableOpacity>
                </Link>
                {/* This lock hardware only supports remote open — closing/sealing is a
                    manual, on-site action, so a remote close command can never succeed
                    (confirmed against real hardware, not a placeholder assumption). */}
                <View className="bg-gray-100 rounded-lg py-3 items-center mt-2">
                  <Text className="text-gray-400 font-medium text-sm">Close — manual only (on-site)</Text>
                </View>
              </>
            ) : (
              <Text className="text-sm text-gray-400 mt-3">No lock device assigned to this shutter yet.</Text>
            )}
          </View>
        );
      }}
      ListEmptyComponent={<Text className="text-center text-gray-500 mt-8">No shutters at this godown yet.</Text>}
    />
  );
}
