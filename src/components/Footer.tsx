export default function Footer() {
  return (
    <footer className="bg-blue-900 text-white py-8">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Company Info */}
          <div>
            <h3 className="text-xl font-bold mb-4">DAD&apos;S CARE Logistics Solutions Pvt. Ltd.</h3>
            <p className="text-gray-300 mb-2">
              <strong>GSTIN:</strong> 09AALCD4009J1ZI
            </p>
            <p className="text-gray-300">
              <strong>Address:</strong> A-15/2, Sulabh Awas Yojana, Transport Nagar, Lucknow, U.P. – 226012
            </p>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="text-xl font-bold mb-4">Contact Us</h3>
            <p className="text-gray-300 mb-2">
              <strong>Phone:</strong> +91 83093 24525
            </p>
            <p className="text-gray-300">
              <strong>Email:</strong> info@dads-care.com
            </p>
          </div>
        </div>

        {/* Copyright */}
        <div className="border-t border-blue-700 mt-8 pt-4 text-center text-gray-300">
          <p>
            © 2025 All Rights Reserved | Privacy Policy | Terms
          </p>
        </div>
      </div>
    </footer>
  );
}
