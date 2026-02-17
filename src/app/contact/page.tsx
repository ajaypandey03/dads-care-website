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
              <div className="text-4xl mb-3">📞</div>
              <h3 className="font-semibold text-gray-800 mb-2">Phone</h3>
              <a href="tel:+918309324525" className="text-blue-700 hover:text-blue-900">
                +91 83093 24525
              </a>
            </div>
            <div className="text-center p-6 bg-blue-50 rounded-lg">
              <div className="text-4xl mb-3">📧</div>
              <h3 className="font-semibold text-gray-800 mb-2">Email</h3>
              <a href="mailto:info@dads-care.com" className="text-blue-700 hover:text-blue-900">
                info@dads-care.com
              </a>
            </div>
            <div className="text-center p-6 bg-blue-50 rounded-lg">
              <div className="text-4xl mb-3">🏢</div>
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
