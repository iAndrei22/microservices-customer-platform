import { apiFetch } from "@/lib/api";

export type CreateCheckoutSessionInput = {
  customerId: string;
  serviceType: string;
  amount: number; // amount in minor units (cents)
};

export type CreateCheckoutSessionResponse = {
  checkoutUrl: string;
  sessionId: string;
};

export type CheckPaymentStatusInput = {
  customerId: string;
  checkoutSessionId: string;
  serviceType: string;
  amount: number;
};

export type CheckPaymentStatusResponse = {
  paid: boolean;
};

async function readErrorMessage(
  response: Response,
  fallback: string
): Promise<string> {
  try {
    const data = await response.json();
    if (data && typeof data === "object" && "message" in data) {
      const message = (data as { message: unknown }).message;
      if (typeof message === "string" && message.trim()) {
        return message;
      }
    }
  } catch {
    // response body may not be JSON
  }
  return fallback;
}

export async function createCheckoutSession(
  payload: CreateCheckoutSessionInput
): Promise<CreateCheckoutSessionResponse> {
  const { customerId, serviceType, amount } = payload;

  const response = await apiFetch(
    `/api/customers/${encodeURIComponent(customerId)}/checkout`,
    {
      method: "POST",
      body: JSON.stringify({ serviceType, amount }),
    }
  );

  if (!response.ok) {
    throw new Error(
      await readErrorMessage(response, "Failed to create checkout session")
    );
  }

  const data = (await response.json()) as unknown;
  const record = data as Record<string, unknown>;

  return {
    checkoutUrl: String(record.checkoutUrl ?? record.checkout_url ?? ""),
    sessionId: String(record.sessionId ?? record.session_id ?? ""),
  };
}

export async function checkPaymentStatus(
  payload: CheckPaymentStatusInput
): Promise<CheckPaymentStatusResponse> {
  const { customerId, checkoutSessionId, serviceType, amount } = payload;

  const response = await apiFetch(
    `/api/customers/${encodeURIComponent(
      customerId
    )}/payment/success`,
    {
      method: "POST",
      body: JSON.stringify({
        checkoutSessionId,
        serviceType,
        amount,
      }),
    }
  );

  if (!response.ok) {
    throw new Error(
      await readErrorMessage(response, "Failed to validate payment")
    );
  }

  const data = (await response.json()) as unknown;
  const record = data as Record<string, unknown>;

  return {
    paid: Boolean(record.paid),
  };
}

export async function simulatePosPayment(
  payload: Pick<CreateCheckoutSessionInput, "customerId" | "serviceType" | "amount">
): Promise<CheckPaymentStatusResponse & { sessionId?: string } > {
  const { customerId, serviceType, amount } = payload;

  const response = await apiFetch(
    `/api/customers/${encodeURIComponent(customerId)}/payment/pos`,
    {
      method: "POST",
      body: JSON.stringify({ serviceType, amount }),
    }
  );

  if (!response.ok) {
    throw new Error(
      await readErrorMessage(response, "Failed to simulate POS payment")
    );
  }

  const data = (await response.json()) as unknown;
  const record = data as Record<string, unknown>;

  return {
    paid: Boolean(record.paid),
    // include session id if provided by backend
    sessionId: String(record.checkoutSessionId ?? record.checkout_session_id ?? ""),
  } as CheckPaymentStatusResponse & { sessionId?: string };
}

