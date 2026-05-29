const API_BASE_URL = "http://localhost:4004";

export async function apiFetch(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const headers = new Headers(options.headers);
  const isAuthRequest = url.startsWith("/auth/");

  if (typeof window !== "undefined" && !isAuthRequest) {
    const token = localStorage.getItem("token");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });
}
