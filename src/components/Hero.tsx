import Link from 'next/link';

export default function Hero() {
  return (
    <section className="relative bg-gradient-to-br from-blue-800 via-blue-600 to-orange-500 text-white py-20 overflow-hidden">
      <div className="absolute inset-0 opacity-10 pointer-events-none">
        {/* Truck SVG decoration */}
        <svg className="absolute top-10 left-10 w-16 h-16" fill="white" viewBox="0 0 24 24">
          <path d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12"/>
        </svg>
        {/* Rail/train SVG decoration */}
        <svg className="absolute top-20 right-20 w-16 h-16" fill="none" stroke="white" strokeWidth={1.5} viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6A2.25 2.25 0 016 3.75h12A2.25 2.25 0 0120.25 6v12A2.25 2.25 0 0118 20.25H6A2.25 2.25 0 013.75 18V6zM3.75 15.75h16.5M8.25 3.75v16.5m7.5-16.5v16.5" />
        </svg>
        {/* Road/path SVG decoration */}
        <svg className="absolute bottom-20 left-1/4 w-16 h-16" fill="none" stroke="white" strokeWidth={1.5} viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 6.75V15m6-6v8.25m.503 3.498l4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 00-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0z" />
        </svg>
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
