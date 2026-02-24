import { Metadata } from 'next';
import ContactForm from '@/components/ContactForm';
import MapEmbed from '@/components/MapEmbed';

export const metadata: Metadata = {
  title: 'Contact Us - DAD\'S CARE Logistics',
  description: 'Get in touch with DAD\'S CARE Logistics Solutions. We\'re here to help with your logistics needs.',
};

export default function ContactPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-blue-700 to-blue-900 text-white py-16">
        <div className="container mx-auto px-4">
          <div className="max-w-3xl mx-auto text-center">
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
              Connect with Confidence — Let&apos;s Move Forward Together
            </h1>
            <p className="text-xl text-blue-100">
              Share your vision. We&apos;ll craft the path.
            </p>
          </div>
        </div>
      </section>

      {/* Contact Details */}
      <section className="py-12 bg-white">
        <div className="container mx-auto px-4">
          <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center p-6 bg-blue-50 rounded-lg">
              <div className="flex justify-center mb-3">
                <svg className="w-10 h-10 text-blue-700" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
                </svg>
              </div>
              <h3 className="font-semibold text-gray-800 mb-2">Phone</h3>
              <a href="tel:+918309324525" className="text-blue-700 hover:text-blue-900">
                +91 83093 24525
              </a>
            </div>
            <div className="text-center p-6 bg-blue-50 rounded-lg">
              <div className="flex justify-center mb-3">
                <svg className="w-10 h-10 text-blue-700" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
                </svg>
              </div>
              <h3 className="font-semibold text-gray-800 mb-2">Email</h3>
              <a href="mailto:info@dads-care.com" className="text-blue-700 hover:text-blue-900">
                info@dads-care.com
              </a>
            </div>
            <div className="text-center p-6 bg-blue-50 rounded-lg">
              <div className="flex justify-center mb-3">
                <svg className="w-10 h-10 text-blue-700" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 21h19.5m-18-18v18m10.5-18v18m6-13.5V21M6.75 6.75h.75m-.75 3h.75m-.75 3h.75m3-6h.75m-.75 3h.75m-.75 3h.75M6.75 21v-3.375c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21M3 3h12m-.75 4.5H21m-3.75 3.75h.008v.008h-.008v-.008zm0 3h.008v.008h-.008v-.008zm0 3h.008v.008h-.008v-.008z" />
                </svg>
              </div>
              <h3 className="font-semibold text-gray-800 mb-2">Address</h3>
              <p className="text-gray-600 text-sm">
                A-15/2, Sulabh Awas Yojana<br />
                Transport Nagar<br />
                Lucknow, U.P. – 226012
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Contact Form and Map */}
      <section className="py-12">
        <div className="container mx-auto px-4">
          <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
            <ContactForm />
            <MapEmbed />
          </div>
        </div>
      </section>
    </div>
  );
}
