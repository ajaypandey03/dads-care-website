import { listSites } from "@/api/sitesApi";
import { useQuery } from "@tanstack/react-query";
import { Link } from "expo-router";
import { ActivityIndicator, FlatList, Text, TouchableOpacity, View } from "react-native";

export default function SitesScreen() {
  const { data: sites, isLoading, isError, refetch, isRefetching } = useQuery({
    queryKey: ["sites"],
    queryFn: listSites,
  });

  return (
    <View className="flex-1 bg-gray-50">
      <Link href="/(app)/alerts" asChild>
        <TouchableOpacity className="bg-white border-b border-gray-200 px-4 py-3">
          <Text className="text-brand font-medium">View recent alerts →</Text>
        </TouchableOpacity>
      </Link>

      {isLoading ? (
        <ActivityIndicator className="mt-8" size="large" color="#1E40AF" />
      ) : isError ? (
        <Text className="text-center text-red-600 mt-8">Couldn&apos;t load your godowns. Pull down to retry.</Text>
      ) : (
        <FlatList
          data={sites}
          keyExtractor={(site) => String(site.id)}
          refreshing={isRefetching}
          onRefresh={refetch}
          contentContainerClassName="p-4"
          ItemSeparatorComponent={() => <View className="h-3" />}
          renderItem={({ item: site }) => (
            <Link href={{ pathname: "/(app)/sites/[siteId]", params: { siteId: site.id } }} asChild>
              <TouchableOpacity className="bg-white rounded-xl p-4 border border-gray-200">
                <Text className="text-lg font-semibold text-gray-900">{site.name}</Text>
                <Text className="text-sm text-gray-500 mt-1">Godown {site.godownCode}</Text>
              </TouchableOpacity>
            </Link>
          )}
          ListEmptyComponent={<Text className="text-center text-gray-500 mt-8">No godowns assigned yet.</Text>}
        />
      )}
    </View>
  );
}
