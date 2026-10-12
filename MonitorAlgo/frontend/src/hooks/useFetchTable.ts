import { useEffect, useState } from "react";

export function useFetchTable<T extends Record<string, any>>(endpoint: string) {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    fetch(endpoint)
      .then(res => res.json())
      .then(json => setData(Array.isArray(json) ? (json as T[]) : []))
      .finally(() => setLoading(false));
  }, [endpoint]);

  return { data, loading };
}
