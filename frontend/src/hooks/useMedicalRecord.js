import { useCallback, useEffect, useState } from 'react';
import { clinicApi } from '../api/clinicApi.js';

/** Loads the full medical record (GET /historia/{id}), which consolidates the five subsystems. */
export default function useMedicalRecord(patientId) {
  const [record, setRecord] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setRecord(await clinicApi.getMedicalRecord(patientId));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [patientId]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { record, error, loading, reload };
}
