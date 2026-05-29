import { apiFetch } from "@/lib/api";
import type {
  CreateCustomerInput,
  Customer,
  UpdateCustomerPayload,
} from "@/types/customer";

function buildCreatePayload(input: CreateCustomerInput) {
  return {
    ...input,
    registeredDate: new Date().toISOString().slice(0, 10),
  };
}

const CUSTOMERS_PATH = "/api/customers";

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

function formatDateValue(value: unknown): string | undefined {
  if (typeof value === "string" && value) {
    return value.slice(0, 10);
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value;
    return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
  }
  return undefined;
}

export function formatDisplayDate(value?: string): string {
  if (!value) return "—";
  return value.slice(0, 10);
}

function normalizeCustomer(raw: unknown): Customer {
  const item = raw as Record<string, unknown>;
  return {
    id: String(item.id ?? ""),
    name: String(item.name ?? ""),
    email: String(item.email ?? ""),
    address: String(item.address ?? ""),
    dateOfBirth:
      formatDateValue(item.dateOfBirth ?? item.date_of_birth) ?? "",
  };
}

function parseCustomers(data: unknown): Customer[] {
  let list: unknown[] = [];
  if (Array.isArray(data)) list = data;
  else if (data && typeof data === "object" && "content" in data) {
    const content = (data as { content: unknown }).content;
    if (Array.isArray(content)) list = content;
  }
  return list.map(normalizeCustomer);
}

export async function getCustomers(): Promise<Customer[]> {
  const response = await apiFetch(CUSTOMERS_PATH);
  if (!response.ok) {
    throw new Error("Failed to load customers");
  }
  const data = await response.json();
  return parseCustomers(data);
}

export async function createCustomer(
  input: CreateCustomerInput
): Promise<Customer> {
  const response = await apiFetch(CUSTOMERS_PATH, {
    method: "POST",
    body: JSON.stringify(buildCreatePayload(input)),
  });
  if (!response.ok) {
    throw new Error(
      await readErrorMessage(response, "Failed to create customer")
    );
  }
  return normalizeCustomer(await response.json());
}

export async function updateCustomer(
  id: string,
  payload: UpdateCustomerPayload
): Promise<Customer> {
  const response = await apiFetch(`${CUSTOMERS_PATH}/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("Failed to update customer");
  }
  return normalizeCustomer(await response.json());
}

export async function deleteCustomer(id: string): Promise<void> {
  const response = await apiFetch(`${CUSTOMERS_PATH}/${id}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error("Failed to delete customer");
  }
}
