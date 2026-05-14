import React, { useEffect, useState, useCallback } from 'react';
import { Filter, X } from 'lucide-react';
import api from '../services/api';

const SEVERITIES = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const ALERT_STATUSES = ['ALL', 'OPEN', 'UNDER_REVIEW', 'RESOLVED', 'FALSE_POSITIVE'];

const SEVERITY_COLORS: Record<string, { bg: string; text: string; border: string }> = {
    LOW:      { bg: 'rgba(34,197,94,0.12)',  text: '#22c55e', border: 'rgba(34,197,94,0.3)' },
    MEDIUM:   { bg: 'rgba(234,179,8,0.12)',  text: '#eab308', border: 'rgba(234,179,8,0.3)' },
    HIGH:     { bg: 'rgba(249,115,22,0.12)', text: '#f97316', border: 'rgba(249,115,22,0.3)' },
    CRITICAL: { bg: 'rgba(239,68,68,0.12)',  text: '#ef4444', border: 'rgba(239,68,68,0.3)' },
};

const Alerts: React.FC = () => {
    const [alerts, setAlerts] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [filters, setFilters] = useState({ severity: '', status: '', startDate: '', endDate: '' });
    const [appliedFilters, setAppliedFilters] = useState(filters);

    const fetchAlerts = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({ page: String(page), size: '15', sort: 'createdAt,desc' });
            if (appliedFilters.severity && appliedFilters.severity !== 'ALL') params.append('severity', appliedFilters.severity);
            if (appliedFilters.status && appliedFilters.status !== 'ALL') params.append('status', appliedFilters.status);
            if (appliedFilters.startDate) params.append('startDate', new Date(appliedFilters.startDate).toISOString());
            if (appliedFilters.endDate) params.append('endDate', new Date(appliedFilters.endDate).toISOString());
            const res = await api.get(`/fraud-alerts?${params}`);
            const data = res.data.data || res.data;
            setAlerts(data.content || []);
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch (err) {
            console.error('Failed to fetch alerts', err);
        } finally {
            setLoading(false);
        }
    }, [page, appliedFilters]);

    useEffect(() => { fetchAlerts(); }, [fetchAlerts]);

    const handleApplyFilters = () => { setAppliedFilters(filters); setPage(0); };
    const handleClearFilters = () => {
        const empty = { severity: '', status: '', startDate: '', endDate: '' };
        setFilters(empty); setAppliedFilters(empty); setPage(0);
    };

    const handleResolve = async (id: number) => {
        try {
            await api.put(`/fraud-alerts/${id}/resolve`, { notes: 'Resolved via dashboard', falsePositive: false });
            fetchAlerts();
        } catch (err) { console.error(err); }
    };

    const handleFalsePositive = async (id: number) => {
        try {
            await api.put(`/fraud-alerts/${id}/false-positive`, { notes: 'Marked false positive via dashboard', falsePositive: true });
            fetchAlerts();
        } catch (err) { console.error(err); }
    };

    const SeverityBadge = ({ severity }: { severity: string }) => {
        const c = SEVERITY_COLORS[severity] || { bg: 'rgba(100,116,139,0.15)', text: '#94a3b8', border: 'rgba(100,116,139,0.3)' };
        return (
            <span style={{ padding: '2px 10px', borderRadius: '12px', fontSize: '12px', background: c.bg, color: c.text, border: `1px solid ${c.border}`, fontWeight: 600 }}>
                {severity}
            </span>
        );
    };

    return (
        <div>
            <div style={{ marginBottom: '24px' }}>
                <h1 style={{ margin: 0 }}>Security Alerts Queue</h1>
                <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>
                    {totalElements.toLocaleString()} total alerts — analyst investigation queue
                </p>
            </div>

            {/* Filter Bar */}
            <div className="glass-panel" style={{ marginBottom: '20px', display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'flex-end' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', fontSize: '13px', marginRight: '4px' }}>
                    <Filter size={16} /> Filters
                </div>
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>Severity</label>
                    <select className="form-control" style={{ minWidth: '140px', padding: '7px 12px' }} value={filters.severity} onChange={e => setFilters(f => ({ ...f, severity: e.target.value }))}>
                        {SEVERITIES.map(s => <option key={s} value={s === 'ALL' ? '' : s}>{s}</option>)}
                    </select>
                </div>
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>Status</label>
                    <select className="form-control" style={{ minWidth: '155px', padding: '7px 12px' }} value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}>
                        {ALERT_STATUSES.map(s => <option key={s} value={s === 'ALL' ? '' : s}>{s}</option>)}
                    </select>
                </div>
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>Start Date</label>
                    <input type="date" className="form-control" style={{ padding: '7px 12px' }} value={filters.startDate} onChange={e => setFilters(f => ({ ...f, startDate: e.target.value }))} />
                </div>
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>End Date</label>
                    <input type="date" className="form-control" style={{ padding: '7px 12px' }} value={filters.endDate} onChange={e => setFilters(f => ({ ...f, endDate: e.target.value }))} />
                </div>
                <div style={{ display: 'flex', gap: '8px', marginTop: 'auto' }}>
                    <button className="btn btn-primary" onClick={handleApplyFilters} style={{ padding: '8px 16px' }}>Apply</button>
                    <button className="btn btn-outline" onClick={handleClearFilters} style={{ padding: '8px 12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <X size={14} /> Clear
                    </button>
                </div>
            </div>

            <div className="glass-panel" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '48px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading alerts...</div>
                ) : (
                    <>
                        <div style={{ overflowX: 'auto' }}>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Alert ID</th>
                                        <th>Transaction</th>
                                        <th>Amount</th>
                                        <th>Severity</th>
                                        <th>Risk Score</th>
                                        <th>Status</th>
                                        <th>Created</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {alerts.length === 0 ? (
                                        <tr><td colSpan={8} style={{ textAlign: 'center', padding: '48px', color: 'var(--text-secondary)' }}>No alerts found.</td></tr>
                                    ) : alerts.map((alert: any) => (
                                        <tr key={alert.id}>
                                            <td style={{ fontFamily: 'monospace', color: 'var(--text-secondary)' }}>#{alert.id}</td>
                                            <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>#{alert.transaction?.id}</td>
                                            <td style={{ fontWeight: 500 }}>₹{Number(alert.transaction?.amount || 0).toLocaleString('en-IN')}</td>
                                            <td><SeverityBadge severity={alert.severity} /></td>
                                            <td>
                                                <span style={{ fontFamily: 'monospace', fontWeight: 600, color: alert.riskScore >= 81 ? '#ef4444' : alert.riskScore >= 51 ? '#f97316' : '#eab308' }}>
                                                    {alert.riskScore}
                                                </span>
                                            </td>
                                            <td>
                                                <span style={{ padding: '2px 10px', borderRadius: '12px', fontSize: '12px', background: 'rgba(100,116,139,0.15)', color: '#94a3b8', border: '1px solid rgba(100,116,139,0.3)' }}>
                                                    {alert.status}
                                                </span>
                                            </td>
                                            <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{new Date(alert.createdAt).toLocaleString()}</td>
                                            <td>
                                                {alert.status !== 'RESOLVED' && alert.status !== 'FALSE_POSITIVE' && (
                                                    <div style={{ display: 'flex', gap: '6px' }}>
                                                        <button
                                                            onClick={() => handleResolve(alert.id)}
                                                            style={{ padding: '4px 10px', fontSize: '12px', background: 'rgba(34,197,94,0.12)', color: '#22c55e', border: '1px solid rgba(34,197,94,0.3)', borderRadius: '6px', cursor: 'pointer' }}
                                                        >Resolve</button>
                                                        <button
                                                            onClick={() => handleFalsePositive(alert.id)}
                                                            style={{ padding: '4px 10px', fontSize: '12px', background: 'rgba(100,116,139,0.12)', color: '#94a3b8', border: '1px solid rgba(100,116,139,0.3)', borderRadius: '6px', cursor: 'pointer' }}
                                                        >False +</button>
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        {totalPages > 1 && (
                            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', padding: '16px', borderTop: '1px solid var(--panel-border)' }}>
                                <button className="btn btn-outline" style={{ padding: '6px 14px', fontSize: '13px' }} disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
                                <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>Page {page + 1} of {totalPages}</span>
                                <button className="btn btn-outline" style={{ padding: '6px 14px', fontSize: '13px' }} disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default Alerts;
