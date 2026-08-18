import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export const metadata: Metadata = {
  title: "DAD'S CARE - Trusted Logistics Solutions",
  description: "DAD'S CARE Logistics Solutions Pvt. Ltd. delivers integrated logistics combining physical execution, digital intelligence, and people-driven precision across India.",
  keywords: "logistics, transport, warehousing, IIoT, AI, supply chain, India logistics",
  openGraph: {
    title: "DAD'S CARE - Trusted Logistics Solutions",
    description: "End-to-end logistics with precision across India",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="scroll-smooth">
      <body className="antialiased">
        <Header />
        <main>{children}</main>
        <Footer />
      </body>
    </html>
  );
}
