import Link from 'next/link';

export default function CallToAction() {
  return (
    <section className="py-16 bg-gradient-to-r from-blue-700 to-blue-900 text-white">
      <div className="container mx-auto px-4">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Ready to Move Forward With Confidence?
          </h2>
          <p className="text-xl mb-8 text-blue-100">
            Let&apos;s build your next-generation logistics backbone.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/contact"
              className="bg-white hover:bg-gray-100 text-blue-800 font-semibold px-8 py-3 rounded-lg transition-colors shadow-lg"
            >
              Contact Us
            </Link>
            <Link
              href="/contact"
              className="bg-transparent hover:bg-blue-800 text-white font-semibold px-8 py-3 rounded-lg transition-colors border-2 border-white"
            >
              Send Inquiry
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}
