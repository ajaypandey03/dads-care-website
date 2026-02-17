import Link from 'next/link';

export default function Hero() {
  return (
    <section className="relative bg-gradient-to-br from-blue-800 via-blue-600 to-orange-500 text-white py-20 overflow-hidden">
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-10 left-10 text-6xl">🚚</div>
        <div className="absolute top-20 right-20 text-6xl">🚂</div>
        <div className="absolute bottom-20 left-1/4 text-6xl">🛣️</div>
      </div>

      <div className="container mx-auto px-4 relative z-10">
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-4xl md:text-6xl font-bold mb-6 animate-fade-in">
            DAD&apos;S CARE: Trusted Logistics, Seamless Solutions
          </h1>
          
          <p className="text-lg md:text-xl mb-8 text-blue-50 max-w-3xl mx-auto leading-relaxed">
            In a world of motion, we deliver certainty. Founded in 2025, DAD&apos;S CARE orchestrates 
            end-to-end logistics with precision — from physical transport and secure storage to AI- and 
            IIoT-driven efficiencies that elevate your operational edge.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/contact"
              className="bg-blue-700 hover:bg-blue-800 text-white font-semibold px-8 py-3 rounded-lg transition-colors shadow-lg"
            >
              Partner With Us
            </Link>
            <Link
              href="#"
              className="bg-white hover:bg-gray-100 text-blue-800 font-semibold px-8 py-3 rounded-lg transition-colors shadow-lg border-2 border-white"
            >
              Download Profile
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}
