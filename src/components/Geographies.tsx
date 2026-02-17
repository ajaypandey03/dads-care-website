export default function Geographies() {
  const regions = {
    North: ['Uttar Pradesh', 'Bihar'],
    'Central & West': ['Madhya Pradesh', 'Maharashtra', 'Gujarat'],
    South: ['Telangana', 'Andhra Pradesh', 'Karnataka'],
  };

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
          {/* India Map */}
          <div className="flex justify-center">
            <div className="relative">
              <svg
                viewBox="0 0 400 500"
                className="w-full max-w-md h-auto"
              >
                {/* India Map Outline */}
                <path
                  d="M200 50 L220 60 L240 70 L250 90 L260 110 L265 130 L270 150 L275 170 L280 190 L285 210 L290 230 L290 250 L285 270 L280 290 L270 310 L260 330 L250 350 L240 365 L230 375 L220 382 L210 388 L200 392 L190 388 L180 382 L170 375 L160 365 L150 350 L140 330 L130 310 L120 290 L115 270 L110 250 L110 230 L115 210 L120 190 L125 170 L130 150 L135 130 L140 110 L150 90 L160 70 L180 60 Z"
                  fill="#E5E7EB"
                  stroke="#9CA3AF"
                  strokeWidth="2"
                />

                {/* Highlighted States (Simplified representation) */}
                {/* Uttar Pradesh */}
                <ellipse cx="200" cy="150" rx="40" ry="30" fill="#1E40AF" opacity="0.7" />
                
                {/* Bihar */}
                <ellipse cx="240" cy="170" rx="25" ry="20" fill="#1E40AF" opacity="0.7" />
                
                {/* Madhya Pradesh */}
                <ellipse cx="180" cy="200" rx="35" ry="25" fill="#F97316" opacity="0.7" />
                
                {/* Maharashtra */}
                <ellipse cx="160" cy="250" rx="35" ry="30" fill="#F97316" opacity="0.7" />
                
                {/* Gujarat */}
                <ellipse cx="130" cy="220" rx="30" ry="25" fill="#F97316" opacity="0.7" />
                
                {/* Telangana */}
                <ellipse cx="200" cy="300" rx="25" ry="20" fill="#1E40AF" opacity="0.7" />
                
                {/* Andhra Pradesh */}
                <ellipse cx="220" cy="320" rx="30" ry="25" fill="#1E40AF" opacity="0.7" />
                
                {/* Karnataka */}
                <ellipse cx="170" cy="320" rx="30" ry="30" fill="#1E40AF" opacity="0.7" />

                {/* Hyderabad Marker */}
                <circle cx="200" cy="300" r="6" fill="#EF4444" stroke="white" strokeWidth="2" />
                <text x="200" y="285" fontSize="12" fill="#EF4444" textAnchor="middle" fontWeight="bold">
                  Hyderabad
                </text>
                <text x="200" y="330" fontSize="10" fill="#DC2626" textAnchor="middle">
                  Live Operations
                </text>
              </svg>
            </div>
          </div>

          {/* Region List */}
          <div>
            <div className="space-y-6">
              {Object.entries(regions).map(([region, states]) => (
                <div key={region}>
                  <h3 className="text-xl font-semibold text-gray-800 mb-2">
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
