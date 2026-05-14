import React, { useEffect, useState, useCallback } from 'react';
import { PlusCircle, Trash2, X } from 'lucide-react';
import api from '../../services/api';

const BlacklistManagement: React.FC = () => {
    const [accounts, setAccounts] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [showModal, setShowModal] = useState(false);
    const [confirmDelete, setConfirmDelete] = useState<number | null>(null);
    const [form, setForm] = useState({ accountNumber: '', reason: '' });
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState('');

    const fetchAccounts = useCallback(async () => {
        setLoading(true);
        try {
            const res = await api.get(`/admin/blacklist?page=${page}&size=10`);
            setAccounts(res.data.data?.content || []);
            setTotalPages(res.data.data?.totalPages || 0);
        } catch (err) {
            console.error('Failed to fetch blacklist', err);
        } finally {
            setLoading(false);
        }
    }, [page]);

    useEffect(() => { fetchAccounts(); }, [fetchAccounts]);

    const handleAdd = async (e: React.FormEvent) => {
        e.preventDefault();
        setFormError('');
        if (!form.accountNumber.trim() || !form.reason.trim()) return setFormError('All fields are required.');
        setSubmitting(true);
        try {
            await api.post('/admin/blacklist', form);
            setShowModal(false);
            setForm({ accountNumber: '', reason: '' });
            fetchAccounts();
        } catch (err: any) {
            setFormError(err.response?.data?.message || 'Failed to add account.');
        } finally {
            setSubmitting(false);
        }
    };

    const handleRemove = async (id: number) => {
        try {
            await api.delete(`/admin/blacklist/${id}`);
            setConfirmDelete(null);
            fetchAccounts();
        } catch (err) {
            console.error('Failed to remove from blacklist', err);
        }
    };

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
                <div>
                    <h1 style={{ margin: 0 }}>Blacklist Management</h1>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Manage globally banned account numbers.</p>
                </div>
                <button className="btn btn-primary" onClick={() => setShowModal(true)} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <PlusCircle size={18} /> Add to Blacklist
                </button>
            </div>

            <div className="glass-panel" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading...</div>
                ) : (
                    <>
                        <div style={{ overflowX: 'auto' }}>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Account Number</th>
                                        <th>Reason</th>
                                        <th>Added At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {accounts.length === 0 ? (
                                        <tr><td colSpan={4} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-secondary)' }}>No blacklisted accounts.</td></tr>
                                    ) : accounts.map((acc: any) => (
                                        <tr key={acc.id}>
                                            <td style={{ fontFamily: 'monospace', fontWeight: 500 }}>{acc.accountNumber}</td>
                                            <td style={{ color: 'var(--text-secondary)', maxWidth: '300px' }}>{acc.reason}</td>
                                            <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{acc.createdAt ? new Date(acc.createdAt).toLocaleDateString() : '—'}</td>
                                            <td>
                                                <button
                                                    onClick={() => setConfirmDelete(acc.id)}
                                                    style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.2)', color: 'var(--danger-color)', padding: '5px 12px', borderRadius: '6px', cursor: 'pointer', fontSize: '12px' }}
                                                >
                                                    <Trash2 size={14} /> Remove
                                                </button>
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

            {/* Add Modal */}
            {showModal && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div className="glass-panel" style={{ width: '100%', maxWidth: '440px', padding: '32px', position: 'relative' }}>
                        <button onClick={() => setShowModal(false)} style={{ position: 'absolute', top: '16px', right: '16px', background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}>
                            <X size={20} />
                        </button>
                        <h3 style={{ margin: '0 0 24px 0' }}>Add Account to Blacklist</h3>
                        {formError && (
                            <div style={{ background: 'rgba(239,68,68,0.1)', color: '#fca5a5', padding: '10px 12px', borderRadius: '6px', marginBottom: '16px', fontSize: '13px' }}>{formError}</div>
                        )}
                        <form onSubmit={handleAdd}>
                            <div className="form-group">
                                <label className="form-label">Account Number</label>
                                <input type="text" className="form-control" value={form.accountNumber} onChange={e => setForm(f => ({ ...f, accountNumber: e.target.value }))} placeholder="10-digit account number" required />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Reason</label>
                                <input type="text" className="form-control" value={form.reason} onChange={e => setForm(f => ({ ...f, reason: e.target.value }))} placeholder="e.g. Known money laundering account" required />
                            </div>
                            <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
                                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)} style={{ flex: 1 }}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={submitting} style={{ flex: 1 }}>{submitting ? 'Adding...' : 'Blacklist Account'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Confirm Delete Dialog */}
            {confirmDelete !== null && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div className="glass-panel" style={{ width: '100%', maxWidth: '380px', padding: '32px', textAlign: 'center' }}>
                        <Trash2 size={40} color="var(--danger-color)" style={{ marginBottom: '16px' }} />
                        <h3 style={{ margin: '0 0 8px 0' }}>Confirm Removal</h3>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>Are you sure you want to remove this account from the blacklist? This action cannot be undone.</p>
                        <div style={{ display: 'flex', gap: '12px' }}>
                            <button className="btn btn-outline" onClick={() => setConfirmDelete(null)} style={{ flex: 1 }}>Cancel</button>
                            <button className="btn" onClick={() => handleRemove(confirmDelete)} style={{ flex: 1, background: 'rgba(239,68,68,0.2)', color: 'var(--danger-color)', border: '1px solid rgba(239,68,68,0.3)' }}>Remove</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BlacklistManagement;
