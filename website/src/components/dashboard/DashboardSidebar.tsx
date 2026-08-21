"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import {
  BellIcon,
  BoxIcon,
  BuildingIcon,
  ChartIcon,
  GearIcon,
  HomeIcon,
  LogoutIcon,
  MenuIcon,
  UsersIcon,
  WarehouseIcon,
  XIcon,
} from "./icons";

const LINKS = [
  { href: "/dashboard", label: "Godown Status", icon: HomeIcon },
  { href: "/dashboard/alerts", label: "Alerts", icon: BellIcon },
  { href: "/dashboard/reports", label: "Reports", icon: ChartIcon },
  { href: "/dashboard/admin/godowns", label: "Godowns", icon: WarehouseIcon },
  { href: "/dashboard/admin/masters", label: "Master Data", icon: BoxIcon },
  { href: "/dashboard/admin/users", label: "Team", icon: UsersIcon },
];

const PLATFORM_LINK = { href: "/dashboard/platform/organizations", label: "Platform", icon: BuildingIcon };

function isActive(pathname: string, href: string): boolean {
  return href === "/dashboard" ? pathname === href : pathname.startsWith(href);
}

function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const { me, isPlatformAdmin, logout } = useAuth();
  const links = isPlatformAdmin ? [...LINKS, PLATFORM_LINK] : LINKS;

  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center px-5 py-5 border-b border-white/10">
        <Link href="/dashboard" onClick={onNavigate} className="inline-flex items-center rounded-lg bg-white px-3 py-2">
          <Image
            src="/images/logo.png"
            alt="DAD'S CARE Logistics"
            width={180}
            height={50}
            className="h-9 w-auto"
            priority
          />
        </Link>
      </div>

      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        {links.map((link) => {
          const active = isActive(pathname, link.href);
          const isPlatform = link.href === PLATFORM_LINK.href;
          const Icon = link.icon;
          return (
            <Link
              key={link.href}
              href={link.href}
              onClick={onNavigate}
              className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                active
                  ? isPlatform
                    ? "bg-brand-orange text-white"
                    : "bg-brand-red text-white"
                  : isPlatform
                    ? "text-brand-orange hover:bg-white/5"
                    : "text-slate-300 hover:bg-white/5 hover:text-white"
              }`}
            >
              <Icon className="w-5 h-5 shrink-0" />
              {link.label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-white/10 px-4 py-4">
        {me && (
          <Link
            href="/dashboard/account"
            onClick={onNavigate}
            className="mb-1 flex items-center gap-3 rounded-lg px-3 py-2 -mx-3 hover:bg-white/5 transition-colors"
          >
            <GearIcon className="w-5 h-5 shrink-0 text-slate-400" />
            <span className="min-w-0">
              <span className="block text-sm font-medium text-white truncate">{me.name}</span>
              <span className="block text-xs text-slate-400 truncate">{me.email}</span>
            </span>
          </Link>
        )}
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5 hover:text-white transition-colors"
        >
          <LogoutIcon className="w-5 h-5 shrink-0" />
          Logout
        </button>
      </div>
    </div>
  );
}

export default function DashboardSidebar() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <>
      {/* Desktop: fixed sidebar */}
      <aside className="hidden md:flex md:w-64 md:shrink-0 md:flex-col bg-brand-charcoal">
        <SidebarContent />
      </aside>

      {/* Mobile: slim top bar with a hamburger toggle */}
      <header className="md:hidden sticky top-0 z-40 flex items-center justify-between bg-brand-charcoal px-4 py-3">
        <Link href="/dashboard" className="inline-flex items-center rounded-lg bg-white px-2.5 py-1.5">
          <Image src="/images/logo.png" alt="DAD'S CARE Logistics" width={140} height={40} className="h-7 w-auto" />
        </Link>
        <button
          onClick={() => setMobileOpen(true)}
          aria-label="Open menu"
          className="text-slate-200 p-1"
        >
          <MenuIcon className="w-6 h-6" />
        </button>
      </header>

      {/* Mobile: drawer overlay */}
      {mobileOpen && (
        <div className="md:hidden fixed inset-0 z-50">
          <div className="absolute inset-0 bg-black/50" onClick={() => setMobileOpen(false)} />
          <aside className="absolute inset-y-0 left-0 w-72 bg-brand-charcoal shadow-xl flex flex-col">
            <div className="flex items-center justify-end px-3 pt-3">
              <button
                onClick={() => setMobileOpen(false)}
                aria-label="Close menu"
                className="text-slate-300 p-1"
              >
                <XIcon className="w-6 h-6" />
              </button>
            </div>
            <div className="flex-1 min-h-0">
              <SidebarContent onNavigate={() => setMobileOpen(false)} />
            </div>
          </aside>
        </div>
      )}
    </>
  );
}
