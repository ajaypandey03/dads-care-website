import { listAlerts, submitFeedback } from "@/api/alertsApi";
import { AlertClassification, AlertItem } from "@/types/models";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ActivityIndicator, FlatList, Text, TouchableOpacity, View } from "react-native";

const CLASSIFICATION_LABEL: Record<AlertClassification, string> = {
  CONFIRMED: "Confirmed",
  UNEXPLAINED_HIGH: "⚠️ Unexplained access",
  UNEXPLAINED_VERIFY: "Please verify",
  SUPPRESSED: "Suppressed",
};

const CLASSIFICATION_COLOR: Record<AlertClassification, string> = {
  CONFIRMED: "bg-green-100 text-green-700",
  UNEXPLAINED_HIGH: "bg-red-100 text-red-700",
  UNEXPLAINED_VERIFY: "bg-amber-100 text-amber-700",
  SUPPRESSED: "bg-gray-100 text-gray-500",
};

export default function AlertsScreen() {
  const queryClient = useQueryClient();
  const { data: alerts, isLoading, isError, refetch, isRefetching } = useQuery({
    queryKey: ["alerts"],
    queryFn: listAlerts,
  });

  const feedbackMutation = useMutation({
    mutationFn: ({ alertId, wasCorrect }: { alertId: number; wasCorrect: boolean }) =>
      submitFeedback(alertId, wasCorrect),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["alerts"] }),
  });

  if (isLoading) {
    return <ActivityIndicator className="mt-8" size="large" color="#1E40AF" />;
  }
  if (isError) {
    return <Text className="text-center text-red-600 mt-8">Couldn&apos;t load alerts. Pull down to retry.</Text>;
  }

  return (
    <FlatList
      className="bg-gray-50"
      data={alerts}
      keyExtractor={(alert) => String(alert.id)}
      refreshing={isRefetching}
      onRefresh={refetch}
      contentContainerClassName="p-4"
      ItemSeparatorComponent={() => <View className="h-3" />}
      renderItem={({ item: alert }) => (
        <AlertCard
          alert={alert}
          onFeedback={(wasCorrect) => feedbackMutation.mutate({ alertId: alert.id, wasCorrect })}
        />
      )}
      ListEmptyComponent={<Text className="text-center text-gray-500 mt-8">No alerts yet.</Text>}
    />
  );
}

function AlertCard({ alert, onFeedback }: { alert: AlertItem; onFeedback: (wasCorrect: boolean) => void }) {
  return (
    <View className="bg-white rounded-xl p-4 border border-gray-200">
      <View className="flex-row items-center justify-between">
        <Text className="text-base font-semibold text-gray-900">
          {alert.direction === "ALARM" ? "🚨 Device Alarm" : `Shutter ${alert.direction === "OPEN" ? "Opened" : "Closed"}`}
        </Text>
        <View className={`px-2 py-1 rounded-full ${CLASSIFICATION_COLOR[alert.classification]}`}>
          <Text className="text-xs font-medium">{CLASSIFICATION_LABEL[alert.classification]}</Text>
        </View>
      </View>
      <Text className="text-sm text-gray-500 mt-1">{new Date(alert.createdAt).toLocaleString()}</Text>
      {alert.sequenceCode && <Text className="text-xs text-gray-400 mt-1">Ref: {alert.sequenceCode}</Text>}

      {alert.classification !== "CONFIRMED" && alert.classification !== "SUPPRESSED" && (
        <View className="flex-row mt-3" style={{ gap: 8 }}>
          <TouchableOpacity
            className="flex-1 border border-green-600 rounded-lg py-2 items-center"
            onPress={() => onFeedback(true)}
          >
            <Text className="text-green-700 font-medium text-sm">Correct</Text>
          </TouchableOpacity>
          <TouchableOpacity
            className="flex-1 border border-red-600 rounded-lg py-2 items-center"
            onPress={() => onFeedback(false)}
          >
            <Text className="text-red-700 font-medium text-sm">Not correct</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}
