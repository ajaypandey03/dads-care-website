# DAD'S CARE Logistics Solutions Website

A modern, production-ready static website for **DAD'S CARE Logistics Solutions Pvt. Ltd.** Built with Next.js 14+ (App Router), TypeScript, and Tailwind CSS, configured for static export and optimized for deployment on Vercel's Hobby plan.

## 🚀 Features

- **Next.js 14+** with App Router
- **TypeScript** for type safety
- **Tailwind CSS** for responsive styling
- **Static Export** (`output: 'export'`) for free hosting
- **SEO Optimized** with meta tags and Open Graph
- **Mobile-First** responsive design
- **Smooth animations** and transitions
- **Contact form** ready for integration with Formspree
- **Google Maps** embed for location

## 📋 Pages

### Home Page (`/`)
- Hero section with gradient background
- Services overview (4 core offerings)
- Why Choose Us (6 differentiators)
- Geographies section with India map
- About section
- Call-to-action section

### Contact Page (`/contact`)
- Contact information
- Contact form with validation
- Google Maps embed

## 🛠️ Tech Stack

- **Framework:** Next.js 16.1.6
- **Language:** TypeScript
- **Styling:** Tailwind CSS v4
- **Font:** Inter (Google Fonts)
- **Deployment:** Vercel

## 🏃‍♂️ Getting Started

### Prerequisites

- Node.js 18+ and npm installed
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/ajaypandey03/dads-care-website.git
cd dads-care-website
```

2. Install dependencies:
```bash
npm install
```

3. Run the development server:
```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

## 📦 Building for Production

### Static Export

This project is configured for static export, which generates static HTML/CSS/JS files:

```bash
npm run build
```

The static files will be generated in the `out/` directory.

### Testing the Build Locally

After building, you can serve the static files locally:

```bash
npx serve@latest out
```

## 🚀 Deploying to Vercel

### Option 1: Deploy via Vercel Dashboard (Recommended)

1. Push your code to GitHub
2. Go to [Vercel Dashboard](https://vercel.com/new)
3. Click "Import Project"
4. Select your GitHub repository
5. Vercel will auto-detect Next.js and configure the build settings
6. Click "Deploy"

### Option 2: Deploy via Vercel CLI

1. Install Vercel CLI:
```bash
npm i -g vercel
```

2. Run the deploy command:
```bash
vercel
```

3. Follow the prompts to link your project

4. Deploy to production:
```bash
vercel --prod
```

## 🌐 Custom Domain Setup

### On Vercel:

1. Go to your project dashboard on Vercel
2. Click on "Settings" → "Domains"
3. Add your custom domain (e.g., `dads-care.com`)
4. Vercel will provide DNS records to configure

### DNS Configuration:

Add these records to your domain provider:

**For root domain (dads-care.com):**
- Type: `A`
- Name: `@`
- Value: `76.76.21.21`

**For www subdomain:**
- Type: `CNAME`
- Name: `www`
- Value: `cname.vercel-dns.com`

**Note:** DNS propagation can take up to 48 hours.

## 📧 Setting Up Contact Form with Formspree

1. Go to [Formspree.io](https://formspree.io) and create an account
2. Create a new form and get your form endpoint
3. Update `src/components/ContactForm.tsx`:

```tsx
// Replace this line:
<form onSubmit={handleSubmit}>

// With:
<form action="https://formspree.io/f/YOUR_FORM_ID" method="POST">
```

4. The form will now send submissions to your email

### Environment Variables (Optional)

If you want to use environment variables for form configuration:

1. Create `.env.local`:
```
NEXT_PUBLIC_FORMSPREE_ID=your_form_id_here
```

2. Update the form action:
```tsx
<form action={`https://formspree.io/f/${process.env.NEXT_PUBLIC_FORMSPREE_ID}`} method="POST">
```

## 📁 Project Structure

```
├── public/
│   └── images/          # Static images
├── src/
│   ├── app/
│   │   ├── layout.tsx   # Root layout with Header/Footer
│   │   ├── page.tsx     # Home page
│   │   ├── globals.css  # Global styles
│   │   └── contact/
│   │       └── page.tsx # Contact page
│   └── components/
│       ├── Header.tsx           # Navigation header
│       ├── Footer.tsx           # Site footer
│       ├── Hero.tsx             # Hero section
│       ├── Services.tsx         # Services grid
│       ├── WhyChooseUs.tsx      # Differentiators
│       ├── Geographies.tsx      # India map
│       ├── About.tsx            # About section
│       ├── CallToAction.tsx     # CTA section
│       ├── ContactForm.tsx      # Contact form
│       └── MapEmbed.tsx         # Google Maps
├── next.config.ts       # Next.js configuration
├── tailwind.config.js   # Tailwind configuration
├── tsconfig.json        # TypeScript configuration
└── package.json         # Dependencies
```

## 🎨 Design Theme

- **Primary Color:** Blue (#1E40AF)
- **Accent Color:** Orange (#F97316)
- **Background:** White / Light gray
- **Style:** Clean, corporate, professional

## 📱 Responsive Design

The website is fully responsive and optimized for:
- Mobile devices (320px+)
- Tablets (768px+)
- Desktops (1024px+)
- Large screens (1280px+)

## 🔍 SEO Features

- Semantic HTML
- Meta descriptions
- Open Graph tags
- Structured data ready
- Fast page load times
- Mobile-friendly

## 📄 License

© 2025 DAD'S CARE Logistics Solutions Pvt. Ltd. All Rights Reserved.

## 📞 Contact

**DAD'S CARE Logistics Solutions Pvt. Ltd.**
- **Email:** info@dads-care.com
- **Phone:** +91 83093 24525
- **Address:** A-15/2, Sulabh Awas Yojana, Transport Nagar, Lucknow, U.P. – 226012
- **GSTIN:** 09AALCD4009J1ZI

## 🤝 Support

For technical support or inquiries about the website, please contact: info@dads-care.com
