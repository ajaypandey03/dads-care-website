export default function WhyChooseUs() {
  const reasons = [
    {
      title: 'Rooted in Trust',
      description: 'Care-driven operations with family-like responsibility.',
    },
    {
      title: 'Agile Asset-Light Model',
      description: 'Scale quickly without overheads.',
    },
    {
      title: 'Tech-Forward Execution',
      description: 'IIoT, APIs, dashboards, automation.',
    },
    {
      title: 'Operational Discipline',
      description: 'Teams trained to meet your SOPs, culture and KPIs.',
    },
    {
      title: 'Future-Focused',
      description: 'Innovation-led systems ensuring endurance and efficiency.',
    },
    {
      title: 'Founded on Strong Values',
      description: 'Humility, learning, quality, reliability.',
    },
  ];

  return (
    <section className="py-16 bg-white">
      <div className="container mx-auto px-4">
        <h2 className="text-3xl md:text-4xl font-bold text-center mb-12 text-gray-800">
          What Makes DAD&apos;S CARE Different?
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {reasons.map((reason, index) => (
            <div
              key={index}
              className="flex items-start space-x-4 p-4 rounded-lg hover:bg-blue-50 transition-colors"
            >
              <div className="flex-shrink-0">
                <svg
                  className="w-6 h-6 text-blue-600"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
              <div>
                <h3 className="text-lg font-semibold mb-1 text-gray-800">
                  {reason.title}
                </h3>
                <p className="text-gray-600 text-sm">
                  {reason.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
