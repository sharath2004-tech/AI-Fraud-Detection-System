import React, { useEffect, useState, useCallback, useRef } from 'react';
import { UploadCloud, Filter, X } from 'lucide-react';
import api from '../services/api';

const STATUSES = ['ALL', 'PENDING', 'COMPLETED', 'FLAGGED', 'REVERSED'];

const Transactions: React.FC = () => {
    const [transactions, setTransactions] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const [filters, setFilters] = useState({
        status: '',
        startDate: '',
        endDate: '',
        minAmount: '',
        maxAmount: '',
    });
    const [appliedFilters, setAppliedFilters] = useState(filters);

    const fetchTransactions = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({ page: String(page), size: '15', sort: 'createdAt,desc' });
            if (appliedFilters.status && appliedFilters.status !== 'ALL') params.append('status', appliedFilters.status);
            if (appliedFilters.startDate) params.append('startDate', new Date(appliedFilters.startDate).toISOString());
            if (appliedFilters.endDate) params.append('endDate', new Date(appliedFilters.endDate).toISOString());
            if (appliedFilters.minAmount) params.append('minAmount', appliedFilters.minAmount);
            if (appliedFilters.maxAmount) params.append('maxAmount', appliedFilters.maxAmount);
            const res = await api.get(`/transactions?${params}`);
            const data = res.data.data || res.data;
            setTransactions(data.content || []);
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
        } catch (err) {
            console.error('Failed to fetch transactions', err);
        } finally {
            setLoading(false);
        }
    }, [page, appliedFilters]);

    useEffect(() => { fetchTransactions(); }, [fetchTransactions]);

    const handleApplyFilters = () => { setAppliedFilters(filters); setPage(0); };
    const handleClearFilters = () => {
        const empty = { status: '', startDate: '', endDate: '', minAmount: '', maxAmount: '' };
        setFilters(empty);
        setAppliedFilters(empty);
        setPage(0);
    };

    const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const formData = new FormData();
        formData.append('file', file);
        try {
            setUploading(true);
            await api.post('/transactions/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            fetchTransactions();
        } catch (err) {
            console.error('Failed to upload CSV', err);
        } finally {
            setUploading(false);
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                    <h1 style={{ margin: 0 }}>Transactions History</h1>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>
                        {totalElements.toLocaleString()} total transactions
                    </p>
                </div>
                <div>
                    <input type="file" accept=".csv" style={{ display: 'none' }} ref={fileInputRef} onChange={handleFileUpload} />
                    <button className="btn btn-primary" onClick={() => fileInputRef.current?.click()} disabled={uploading} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <UploadCloud size={18} />
                        {uploading ? 'Uploading...' : 'Bulk CSV Upload'}
                    </button>
                </div>
            </div>

            {/* Filter Bar */}
            <div className="glass-panel" style={{ marginBottom: '20px', display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'flex-end' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', fontSize: '13px', marginRight: '4px' }}>
                    <Filter size={16} /> Filters
                </div>
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>Status</label>
                    <select className="form-control" style={{ minWidth: '130px', padding: '7px 12px' }} value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}>
                        {STATUSES.map(s => <option key={s} value={s === 'ALL' ? '' : s}>{s}</option>)}
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
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>Min Amount (₹)</label>
                    <input type="number" className="form-control" style={{ width: '120px', padding: '7px 12px' }} placeholder="0" value={filters.minAmount} onChange={e => setFilters(f => ({ ...f, minAmount: e.target.value }))} />
                </div>
                <div>
                    <label style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>Max Amount (₹)</label>
                    <input type="number" className="form-control" style={{ width: '120px', padding: '7px 12px' }} placeholder="Any" value={filters.maxAmount} onChange={e => setFilters(f => ({ ...f, maxAmount: e.target.value }))} />
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
                    <div style={{ padding: '48px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading transactions...</div>
                ) : (
                    <>
                        <div style={{ overflowX: 'auto' }}>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Type</th>
                                        <th>Amount</th>
                                        <th>From</th>
                                        <th>To</th>
                                        <th>Status</th>
                                        <th>Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {transactions.length === 0 ? (
                                        <tr><td colSpan={7} style={{ textAlign: 'center', padding: '48px', color: 'var(--text-secondary)' }}>No transactions found.</td></tr>
                                    ) : transactions.map((tx: any) => (
                                        <tr key={tx.id}>
                                            <td style={{ fontFamily: 'monospace', color: 'var(--text-secondary)' }}>#{tx.id}</td>
                                            <td>{tx.transactionType}</td>
                                            <td style={{ fontWeight: 500 }}>₹{Number(tx.amount).toLocaleString('en-IN')}</td>
                                            <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>{tx.senderAccount}</td>
                                            <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>{tx.receiverAccount}</td>
                                            <td>
                                                <span style={{
                                                    padding: '2px 10px', borderRadius: '12px', fontSize: '12px',
                                                    background: tx.status === 'FLAGGED' ? 'rgba(239,68,68,0.15)' : tx.status === 'COMPLETED' ? 'rgba(34,197,94,0.15)' : 'rgba(100,116,139,0.15)',
                                                    color: tx.status === 'FLAGGED' ? '#ef4444' : tx.status === 'COMPLETED' ? '#22c55e' : '#94a3b8',
                                                    border: `1px solid ${tx.status === 'FLAGGED' ? 'rgba(239,68,68,0.3)' : tx.status === 'COMPLETED' ? 'rgba(34,197,94,0.3)' : 'rgba(100,116,139,0.3)'}`
                                                }}>
                                                    {tx.status}
                                                </span>
                                            </td>
                                            <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{new Date(tx.createdAt).toLocaleString()}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        {/* Pagination */}
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

export default Transactions;
