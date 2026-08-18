export default function MapEmbed() {
  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      <h3 className="text-2xl font-bold p-6 text-gray-800 bg-gray-50 border-b">
        Our Location
      </h3>
      <div className="w-full h-96">
        <iframe
          src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3560.5823571382876!2d80.96857331502695!3d26.82544368314456!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x399bfd0000000001%3A0x0!2sTransport%20Nagar%2C%20Lucknow%2C%20Uttar%20Pradesh%20226012!5e0!3m2!1sen!2sin!4v1234567890123!5m2!1sen!2sin"
          width="100%"
          height="100%"
          style={{ border: 0 }}
          allowFullScreen
          loading="lazy"
          referrerPolicy="no-referrer-when-downgrade"
          title="DAD'S CARE Location"
        ></iframe>
      </div>
    </div>
  );
}
