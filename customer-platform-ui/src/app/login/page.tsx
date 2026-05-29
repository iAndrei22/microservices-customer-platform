"use client";

import { useState } from "react";
import { ArrowRight, AtSign, Building2, Lock, Shield } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { apiFetch } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { cn } from "@/lib/utils";

function IconInput({
  id,
  icon: Icon,
  className,
  ...props
}: React.ComponentProps<typeof Input> & {
  icon: React.ComponentType<{ className?: string }>;
}) {
  return (
    <div className="relative">
      <Icon className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />
      <Input id={id} className={cn("h-10 pl-9", className)} {...props} />
    </div>
  );
}

export default function LoginPage() {
  const { login, isLoading: authLoading } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const response = await apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error("Invalid credentials");
      }

      const data = await response.json();
      const token = data.token ?? data.accessToken;

      if (!token) {
        throw new Error("No token returned from server");
      }

      login(token);
    } catch {
      setError("Login failed. Check your credentials and try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-muted/40">
        <p className="text-sm text-muted-foreground">Loading...</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen">
      {/* Brand panel — hidden on small screens */}
      <aside className="relative hidden w-[42%] flex-col justify-between overflow-hidden bg-zinc-900 p-10 text-white lg:flex">
        <div
          className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_80%_60%_at_20%_0%,oklch(0.52_0.13_192/0.35),transparent_60%)]"
          aria-hidden
        />
        <div className="relative animate-enter">
          <div className="flex items-center gap-2.5">
            <div className="flex size-9 items-center justify-center rounded-lg bg-primary/90 text-primary-foreground shadow-lg shadow-primary/30">
              <Building2 className="size-5" />
            </div>
            <span className="text-lg font-semibold tracking-tight">
              Customer Platform
            </span>
          </div>
        </div>
        <div className="relative space-y-6 animate-enter stagger-2">
          <h2 className="max-w-sm text-3xl font-semibold leading-tight tracking-tight">
            Enterprise customer management, simplified.
          </h2>
          <p className="max-w-sm text-sm leading-relaxed text-zinc-400">
            Secure access to your client records. Built for teams that need
            reliability, clarity, and speed.
          </p>
          <ul className="space-y-3 text-sm text-zinc-300">
            <li className="flex items-center gap-2.5">
              <Shield className="size-4 shrink-0 text-primary" />
              Role-based secure authentication
            </li>
            <li className="flex items-center gap-2.5">
              <Building2 className="size-4 shrink-0 text-primary" />
              Full customer lifecycle management
            </li>
          </ul>
        </div>
        <p className="relative text-xs text-zinc-500">
          © {new Date().getFullYear()} Customer Platform
        </p>
      </aside>

      {/* Form panel */}
      <main className="flex flex-1 flex-col items-center justify-center bg-muted/40 px-4 py-10 sm:px-6">
        <div className="mb-8 flex items-center gap-2 lg:hidden">
          <div className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Building2 className="size-4" />
          </div>
          <span className="font-semibold tracking-tight">Customer Platform</span>
        </div>

        <Card className="animate-enter w-full max-w-md border-border/80 bg-card/95 py-6 shadow-lg shadow-zinc-900/5 ring-1 ring-border/60 backdrop-blur-sm stagger-1">
          <CardHeader className="space-y-1 px-6 pb-2">
            <CardTitle className="text-2xl font-semibold tracking-tight">
              Sign in
            </CardTitle>
            <CardDescription className="text-base leading-relaxed">
              Enter your credentials to access the customer platform.
            </CardDescription>
          </CardHeader>
          <CardContent className="px-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-sm font-medium">
                  Email
                </Label>
                <IconInput
                  id="email"
                  icon={AtSign}
                  type="email"
                  placeholder="you@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoComplete="email"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password" className="text-sm font-medium">
                  Password
                </Label>
                <IconInput
                  id="password"
                  icon={Lock}
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                />
              </div>
              {error && (
                <p
                  className="rounded-lg border border-destructive/20 bg-destructive/5 px-3 py-2 text-sm text-destructive"
                  role="alert"
                >
                  {error}
                </p>
              )}
              <Button
                type="submit"
                size="lg"
                className="mt-1 h-10 w-full gap-2 text-sm font-semibold"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  "Signing in..."
                ) : (
                  <>
                    Sign in
                    <ArrowRight className="size-4" />
                  </>
                )}
              </Button>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
