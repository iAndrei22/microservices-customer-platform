"use client";

import { useCallback, useMemo, useState } from "react";
import { ExternalLink, Loader2, Copy } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  createCheckoutSession,
  simulatePosPayment,
  type CreateCheckoutSessionInput,
} from "@/lib/billing";

type PaymentGeneratorModalProps = {
  customerId: string;
  customerName?: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

type GeneratorStep = "form" | "result";

export function PaymentGeneratorModal({
  customerId,
  customerName,
  open,
  onOpenChange,
}: PaymentGeneratorModalProps) {
  const [serviceType, setServiceType] = useState("");
  const [amountRON, setAmountRON] = useState("");
  const [step, setStep] = useState<GeneratorStep>("form");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [checkoutUrl, setCheckoutUrl] = useState("");
  const [sessionId, setSessionId] = useState("");
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const isValidAmount = useMemo(() => {
    if (!amountRON.trim()) return false;
    const parsed = Number(amountRON.replace(",", "."));
    return Number.isFinite(parsed) && parsed > 0;
  }, [amountRON]);

  const resetState = useCallback(() => {
    setServiceType("");
    setAmountRON("");
    setStep("form");
    setIsSubmitting(false);
    setError(null);
    setCheckoutUrl("");
    setSessionId("");
    setToastMessage(null);
  }, []);

  const handleOpenChange = (next: boolean) => {
    onOpenChange(next);
    if (!next) {
      resetState();
    }
  };

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!isValidAmount) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const parsedRON = Number(amountRON.replace(",", "."));
      const amountInCents = Math.round(parsedRON * 100);

      const payload: CreateCheckoutSessionInput = {
        customerId,
        serviceType: serviceType.trim(),
        amount: amountInCents,
      };

      const result = await createCheckoutSession(payload);

      setCheckoutUrl(result.checkoutUrl);
      setSessionId(result.sessionId);
      setStep("result");
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Failed to generate payment link.";
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCopyUrl() {
    if (!checkoutUrl) return;

    try {
      await navigator.clipboard.writeText(checkoutUrl);
      setToastMessage("Link copied to clipboard.");
    } catch {
      setToastMessage("Unable to copy link. Please copy it manually.");
    } finally {
      window.setTimeout(() => setToastMessage(null), 2500);
    }
  }

  function handlePayNow() {
    if (!checkoutUrl || !sessionId) return;
    window.open(checkoutUrl, "_blank", "noopener,noreferrer");
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Generate payment link</DialogTitle>
          <DialogDescription>
            Create a Stripe checkout link for{" "}
            <span className="font-medium text-foreground">
              {customerName ?? "this customer"}
            </span>
            .
          </DialogDescription>
        </DialogHeader>

        {step === "form" ? (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="serviceType">Service type</Label>
              <Input
                id="serviceType"
                value={serviceType}
                onChange={(e) => setServiceType(e.target.value)}
                required
                placeholder="e.g. Consulting session"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="amountRON">Amount (RON)</Label>
              <Input
                id="amountRON"
                type="number"
                min={0}
                step="0.01"
                value={amountRON}
                onChange={(e) => setAmountRON(e.target.value)}
                required
                placeholder="e.g. 150.00"
              />
            </div>

            {error && (
              <p className="text-sm text-destructive" role="alert">
                {error}
              </p>
            )}

            <DialogFooter>
              <Button
                type="submit"
                disabled={isSubmitting || !serviceType.trim() || !isValidAmount}
                className="gap-2"
              >
                {isSubmitting && <Loader2 className="size-4 animate-spin" />}
                Generate link
              </Button>
            </DialogFooter>
          </form>
        ) : (
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="checkoutUrl">Checkout URL</Label>
              <Input
                id="checkoutUrl"
                value={checkoutUrl}
                readOnly
                className="font-mono text-xs"
              />
              <p className="text-[11px] text-muted-foreground">
                Share this link with the customer or open it directly for an on-the-spot
                payment.
              </p>
            </div>

            {toastMessage && (
              <div className="rounded-md border border-border bg-muted px-3 py-2 text-xs text-foreground">
                {toastMessage}
              </div>
            )}

            <div className="flex flex-col gap-2 sm:flex-row sm:justify-between">
              <Button
                type="button"
                variant="outline"
                className="w-full gap-2 sm:w-auto"
                onClick={handleCopyUrl}
              >
                <Copy className="size-4" />
                Copy Link
              </Button>
              <div className="flex gap-2 w-full sm:w-auto">
                <Button
                  type="button"
                  className="flex-1 gap-2"
                  onClick={handlePayNow}
                >
                  <ExternalLink className="size-4" />
                  Pay Online
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  className="flex-1 gap-2"
                  onClick={async () => {
                    if (!sessionId) return;
                    try {
                      // call POS simulator using the same metadata
                      const parsedAmount = Number(amountRON.replace(",", "."));
                      const amountInCents = Math.round(parsedAmount * 100);
                      const result = await simulatePosPayment({
                        customerId,
                        serviceType: serviceType.trim(),
                        amount: amountInCents,
                      });

                      const simSessionId = result.sessionId || sessionId || `sim_pos_${Date.now()}`;

                      // navigate to success page with simulated flag
                      const params = new URLSearchParams({
                        session_id: String(simSessionId),
                        customerId: String(customerId),
                        serviceType: String(serviceType.trim()),
                        amount: String(amountInCents),
                        simulated: "true",
                      });
                      window.location.href = `/success?${params.toString()}`;
                    } catch (err) {
                      setToastMessage(
                        err instanceof Error ? err.message : "POS simulation failed"
                      );
                      window.setTimeout(() => setToastMessage(null), 2500);
                    }
                  }}
                >
                  Pay at POS
                </Button>
              </div>
            </div>

            <p className="text-[11px] text-muted-foreground">
              Session ID:{" "}
              <span className="font-mono text-[11px]">
                {sessionId || "—"}
              </span>
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

