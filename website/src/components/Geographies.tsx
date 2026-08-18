'use client';

import dynamic from 'next/dynamic';

const IndiaMap = dynamic(() => import('./IndiaMap'), {
  ssr: false,
  loading: () => (
    <div className="flex items-center justify-center h-[460px] w-full rounded-xl bg-gray-100 text-gray-400">
      Loading map…
    </div>
  ),
});

const regionConfig: Record<string, { states: string[]; color: string }> = {
  North: { states: ['Uttar Pradesh', 'Bihar'], color: '#2563EB' },
  'Central & West': { states: ['Madhya Pradesh', 'Maharashtra', 'Gujarat'], color: '#F97316' },
  South: { states: ['Telangana', 'Andhra Pradesh', 'Karnataka'], color: '#16A34A' },
};

export default function Geographies() {
  return (
    <section className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        <h2 className="text-3xl md:text-4xl font-bold text-center mb-4 text-gray-800">
          Geographies We Serve
        </h2>
        <p className="text-center text-xl text-gray-600 mb-12">
          Current Presence
        </p>

        <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Interactive India Map */}
          <div className="rounded-xl overflow-hidden shadow-lg">
            <IndiaMap />
          </div>

          {/* Region List */}
          <div>
            <div className="space-y-6">
              {Object.entries(regionConfig).map(([region, { states, color }]) => (
                <div key={region}>
                  <h3 className="text-xl font-semibold text-gray-800 mb-2 flex items-center gap-2">
                    <span
                      className="inline-block w-3 h-3 rounded-full flex-shrink-0"
                      style={{ backgroundColor: color }}
                    />
                    {region}
                  </h3>
                  <ul className="list-disc list-inside space-y-1 text-gray-600">
                    {states.map((state) => (
                      <li key={state}>{state}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>

            <div className="mt-8 p-4 bg-blue-50 rounded-lg border-l-4 border-blue-600">
              <p className="text-gray-700 font-medium">
                We expand wherever your strategy demands — anywhere in India.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
