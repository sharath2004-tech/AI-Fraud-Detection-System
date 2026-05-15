import React, { useEffect, useState, useCallback } from 'react';
import { RefreshCw } from 'lucide-react';
import api from '../../services/api';

const ROLES = ['ADMIN', 'ANALYST', 'USER'];
const STATUSES = ['ACTIVE', 'INACTIVE', 'LOCKED'];

const UserManagement: React.FC = () => {
    const [users, setUsers] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [filters, setFilters] = useState({ role: '', status: '' });

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({ page: String(page), size: '10', sort: 'createdAt,desc' });
            if (filters.role) params.append('role', filters.role);
            if (filters.status) params.append('status', filters.status);
            const res = await api.get(`/admin/users?${params}`);
            setUsers(res.data.data?.content || []);
            setTotalPages(res.data.data?.totalPages || 0);
        } catch (err) {
            console.error('Failed to fetch users', err);
        } finally {
            setLoading(false);
        }
    }, [page, filters]);

    useEffect(() => { fetchUsers(); }, [fetchUsers]);

    const handleRoleChange = async (userId: number, role: string) => {
        try {
            await api.put(`/admin/users/${userId}/role`, { role });
            fetchUsers();
        } catch (err) { console.error(err); }
    };

    const handleStatusChange = async (userId: number, status: string) => {
        try {
            await api.put(`/admin/users/${userId}/status`, { status });
            fetchUsers();
        } catch (err) { console.error(err); }
    };

    const getStatusBadge = (status: string) => {
        const colors: Record<string, string> = { ACTIVE: '#22c55e', INACTIVE: '#64748b', LOCKED: '#ef4444' };
        return (
            <span style={{ padding: '2px 10px', borderRadius: '12px', fontSize: '12px', background: `${colors[status] || '#64748b'}22`, color: colors[status] || '#64748b', border: `1px solid ${colors[status] || '#64748b'}44` }}>
                {status}
            </span>
        );
    };

    return (
        <div>
            <div style={{ marginBottom: '28px' }}>
                <h1 style={{ margin: 0 }}>User Management</h1>
                <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Manage user roles, statuses, and access controls.</p>
            </div>

            {/* Filters */}
            <div className="glass-panel" style={{ marginBottom: '24px', display: 'flex', gap: '16px', flexWrap: 'wrap', alignItems: 'center' }}>
                <select className="form-control" style={{ width: 'auto', minWidth: '150px' }} value={filters.role} onChange={e => { setFilters(f => ({ ...f, role: e.target.value })); setPage(0); }}>
                    <option value="">All Roles</option>
                    {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                </select>
                <select className="form-control" style={{ width: 'auto', minWidth: '160px' }} value={filters.status} onChange={e => { setFilters(f => ({ ...f, status: e.target.value })); setPage(0); }}>
                    <option value="">All Statuses</option>
                    {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
                <button className="btn btn-outline" onClick={() => { setFilters({ role: '', status: '' }); setPage(0); }} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <RefreshCw size={14} /> Clear Filters
                </button>
            </div>

            <div className="glass-panel" style={{ padding: 0, overflow: 'hidden' }}>
                {loading ? (
                    <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading users...</div>
                ) : (
                    <>
                        <div style={{ overflowX: 'auto' }}>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Created At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {users.length === 0 ? (
                                        <tr><td colSpan={6} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-secondary)' }}>No users found.</td></tr>
                                    ) : users.map((user: any) => (
                                        <tr key={user.id}>
                                            <td style={{ fontWeight: 500 }}>{user.name}</td>
                                            <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{user.email}</td>
                                            <td>
                                                <select
                                                    value={user.role}
                                                    onChange={e => handleRoleChange(user.id, e.target.value)}
                                                    style={{ background: 'var(--input-bg)', border: '1px solid var(--panel-border)', color: '#fff', padding: '4px 8px', borderRadius: '6px', fontSize: '13px', cursor: 'pointer' }}
                                                >
                                                    {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                                </select>
                                            </td>
                                            <td>{getStatusBadge(user.status)}</td>
                                            <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{new Date(user.createdAt).toLocaleDateString()}</td>
                                            <td>
                                                <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                                                    {user.status !== 'ACTIVE' && (
                                                        <button className="btn btn-success" style={{ padding: '4px 10px', fontSize: '12px' }} onClick={() => handleStatusChange(user.id, 'ACTIVE')}>Enable</button>
                                                    )}
                                                    {user.status !== 'INACTIVE' && (
                                                        <button className="btn btn-outline" style={{ padding: '4px 10px', fontSize: '12px' }} onClick={() => handleStatusChange(user.id, 'INACTIVE')}>Disable</button>
                                                    )}
                                                    {user.status !== 'LOCKED' && (
                                                        <button className="btn" style={{ padding: '4px 10px', fontSize: '12px', background: 'rgba(239,68,68,0.15)', color: 'var(--danger-color)', border: '1px solid rgba(239,68,68,0.2)' }} onClick={() => handleStatusChange(user.id, 'LOCKED')}>Lock</button>
                                                    )}
                                                </div>
                                            </td>
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

export default UserManagement;
