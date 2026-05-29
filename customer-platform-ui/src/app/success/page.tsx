"use client";

export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { CheckCircle2, Loader2, XCircle, ArrowLeft } from "lucide-react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { checkPaymentStatus } from "@/lib/billing";

type Status = "idle" | "loading" | "success" | "error";

function parsePaymentParams(searchParams: URLSearchParams) {
  const sessionId = searchParams.get("session_id");
  const customerId = searchParams.get("customerId");
  const serviceType = searchParams.get("serviceType");
  const amountRaw = searchParams.get("amount");

  if (!sessionId || !customerId || !serviceType || amountRaw === null) {
    return null;
  }

  const amount = Number(amountRaw);
  if (!Number.isFinite(amount)) {
    return null;
  }

  return {
    sessionId,
    customerId,
    serviceType,
    amount,
  };
}

export default function PaymentSuccessPage() {
  const router = useRouter();
  // read params from window.location inside useEffect to avoid Suspense/prerender issues
  // (we're in a client component)

  const [status, setStatus] = useState<Status>("idle");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const params = parsePaymentParams(new URLSearchParams(window.location.search));
    const isSimulated = new URLSearchParams(window.location.search).get("simulated") === "true";

    if (!params) {
      setStatus("error");
      setError(
        "Missing payment details in the URL. Expected session_id, customerId, serviceType, and amount."
      );
      return;
    }

    let cancelled = false;

    async function validatePayment() {
      setStatus("loading");
      setError(null);

      // If this is a simulated POS payment, skip Stripe validation
      if (isSimulated) {
        if (cancelled) return;
        setStatus("success");
        return;
      }

      try {
        if (!params) {
          setStatus("error");
          setError("Missing payment parameters.");
          return;
        }

        const result = await checkPaymentStatus({
          customerId: params.customerId,
          checkoutSessionId: params.sessionId,
          serviceType: params.serviceType,
          amount: params.amount,
        });

        if (cancelled) return;

        if (result.paid) {
          setStatus("success");
        } else {
          setStatus("error");
          setError("Payment not completed. Please verify the status in Stripe.");
        }
      } catch (err) {
        if (cancelled) return;
        const message =
          err instanceof Error ? err.message : "Unable to validate the payment.";
        setStatus("error");
        setError(message);
      }
    }

    validatePayment();

    return () => {
      cancelled = true;
    };
  }, []);

  const showSpinner = status === "loading" || status === "idle";

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-md border-border/80 bg-card/95 py-6 shadow-lg shadow-zinc-900/5 ring-1 ring-border/60">
        <CardHeader className="items-center space-y-3 pb-4 text-center">
          {showSpinner && (
            <div className="flex size-12 items-center justify-center rounded-full bg-muted">
              <Loader2 className="size-6 animate-spin text-primary" />
            </div>
          )}
          {status === "success" && (
            <div className="flex size-12 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
              <CheckCircle2 className="size-7" />
            </div>
          )}
          {status === "error" && !showSpinner && (
            <div className="flex size-12 items-center justify-center rounded-full bg-destructive/10 text-destructive">
              <XCircle className="size-7" />
            </div>
          )}

          <CardTitle className="text-xl font-semibold">
            {status === "success"
              ? "Payment validated successfully!"
              : showSpinner
                ? "Validating payment..."
                : "Unable to validate payment"}
          </CardTitle>
          <CardDescription>
            {status === "success"
              ? "Your payment has been confirmed and recorded."
              : showSpinner
                ? "Please wait while we confirm the payment with the billing service."
                : (error ?? "Something went wrong while validating this payment.")}
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
