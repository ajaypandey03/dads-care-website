export default function Services() {
  const services = [
    {
      icon: '🚚',
      title: 'Physical Movements',
      description: 'Reliable transport across road & rail — your cargo, delivered with care.',
    },
    {
      icon: '🏬',
      title: 'Distribution & Storage',
      description: 'Optimized warehousing and streamlined distribution for faster turnarounds.',
    },
    {
      icon: '📊',
      title: 'Consulting & Cost Optimization',
      description: 'Structured strategies that reduce operational costs and enhance competitiveness.',
    },
    {
      icon: '🤖',
      title: 'Tech Integration (IIoT + AI)',
      description: 'Smarter operations through automation, tracking, sensing & real-time data.',
    },
  ];

  return (
    <section id="services" className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        <h2 className="text-3xl md:text-4xl font-bold text-center mb-12 text-gray-800">
          Our Core Offerings
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-5xl mx-auto">
          {services.map((service, index) => (
            <div
              key={index}
              className="bg-white p-6 rounded-lg shadow-md hover:shadow-xl transition-shadow duration-300 border border-gray-100"
            >
              <div className="text-5xl mb-4">{service.icon}</div>
              <h3 className="text-xl font-semibold mb-3 text-gray-800">
                {service.title}
              </h3>
              <p className="text-gray-600">
                {service.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
