"use client";

import { ArrowLeft, XCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function PaymentCancelPage() {
  const router = useRouter();

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-md border-border/80 bg-card/95 py-6 shadow-lg shadow-zinc-900/5 ring-1 ring-border/60">
        <CardHeader className="items-center space-y-3 pb-4 text-center">
          <div className="flex size-12 items-center justify-center rounded-full bg-destructive/10 text-destructive">
            <XCircle className="size-7" />
          </div>
          <CardTitle className="text-xl font-semibold">
            Payment canceled
          </CardTitle>
          <CardDescription>
            The payment process was interrupted or cancelled.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex justify-center">
          <Button
            type="button"
            variant="outline"
            className="gap-2"
            onClick={() => router.push("/dashboard")}
          >
            <ArrowLeft className="size-4" />
            Return to Dashboard
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
