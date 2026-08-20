"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

const LINKS = [
  { href: "/dashboard", label: "Godown Status" },
  { href: "/dashboard/alerts", label: "Alerts" },
  { href: "/dashboard/reports", label: "Reports" },
  { href: "/dashboard/admin/masters", label: "Master Data" },
  { href: "/dashboard/admin/users", label: "Team" },
];

const PLATFORM_LINK = { href: "/dashboard/platform/organizations", label: "Platform" };

export default function DashboardNav() {
  const pathname = usePathname();
  const { me, isPlatformAdmin, logout } = useAuth();
  const links = isPlatformAdmin ? [...LINKS, PLATFORM_LINK] : LINKS;

  return (
    <header className="sticky top-0 z-50 bg-white shadow-md">
      <nav className="container mx-auto px-4 py-3">
        <div className="flex items-center justify-between gap-4">
          <Link href="/dashboard" className="font-bold text-blue-800 text-lg shrink-0">
            DAD&apos;S CARE
          </Link>
          <div className="hidden md:flex items-center gap-1 overflow-x-auto">
            {links.map((link) => {
              const active = link.href === "/dashboard" ? pathname === link.href : pathname.startsWith(link.href);
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`px-3 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                    active
                      ? "bg-blue-700 text-white"
                      : link.href === PLATFORM_LINK.href
                        ? "text-orange-700 hover:bg-orange-50"
                        : "text-gray-700 hover:bg-gray-100"
                  }`}
                >
                  {link.label}
                </Link>
              );
            })}
          </div>
          <div className="flex items-center gap-3 shrink-0">
            {me && <span className="hidden sm:inline text-sm text-gray-500">{me.name}</span>}
            <button
              onClick={logout}
              className="text-sm font-medium text-gray-600 hover:text-red-600 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
        <div className="md:hidden flex flex-wrap gap-1 pt-3">
          {links.map((link) => {
            const active = link.href === "/dashboard" ? pathname === link.href : pathname.startsWith(link.href);
            return (
              <Link
                key={link.href}
                href={link.href}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium ${
                  active ? "bg-blue-700 text-white" : "bg-gray-100 text-gray-700"
                }`}
              >
                {link.label}
              </Link>
            );
          })}
        </div>
      </nav>
    </header>
  );
}
