import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Activity, AlertTriangle, ShieldCheck, CreditCard, Users } from 'lucide-react';
import api from '../../services/api';

const AdminDashboard: React.FC = () => {
    const [metrics, setMetrics] = useState<any>(null);
    const [topUsers, setTopUsers] = useState<any[]>([]);
    const [auditLogs, setAuditLogs] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAll = async () => {
            try {
                const [metricsRes, topRes, logsRes] = await Promise.all([
                    api.get('/dashboard/metrics'),
                    api.get('/dashboard/top-flagged-users'),
                    api.get('/admin/audit-logs?size=10&sort=timestamp,desc'),
                ]);
                setMetrics(metricsRes.data.data);
                setTopUsers(topRes.data.data || []);
                setAuditLogs(logsRes.data.data?.content || []);
            } catch (err) {
                console.error('Failed to load admin dashboard', err);
            } finally {
                setLoading(false);
            }
        };
        fetchAll();
    }, []);

    if (loading) {
        return <div style={{ color: 'var(--text-secondary)', padding: '32px' }}>Loading admin dashboard...</div>;
    }

    const statCards = [
        { title: 'Total Transactions', value: metrics?.totalTransactions, icon: <CreditCard size={20} />, color: 'var(--primary-color)' },
        { title: 'Flagged', value: metrics?.flaggedTransactions, icon: <Activity size={20} />, color: '#f97316' },
        { title: 'Unresolved Alerts', value: metrics?.unresolvedAlerts, icon: <AlertTriangle size={20} />, color: 'var(--danger-color)' },
        { title: 'Critical Alerts', value: metrics?.criticalAlerts, icon: <ShieldCheck size={20} />, color: '#a855f7' },
    ];

    return (
        <div>
            <div style={{ marginBottom: '32px' }}>
                <h1 style={{ margin: 0 }}>Admin Dashboard</h1>
                <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>System-wide overview and recent activity.</p>
            </div>

            {/* Stats */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', marginBottom: '32px' }}>
                {statCards.map((card, i) => (
                    <motion.div
                        key={card.title}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: i * 0.1 }}
                        className="glass-panel"
                        style={{ flex: 1, minWidth: '180px', display: 'flex', alignItems: 'center', gap: '16px' }}
                    >
                        <div style={{ width: '44px', height: '44px', borderRadius: '10px', background: card.color, opacity: 0.9, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff' }}>
                            {card.icon}
                        </div>
                        <div>
                            <p style={{ color: 'var(--text-secondary)', fontSize: '12px', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>{card.title}</p>
                            <h3 style={{ fontSize: '24px', margin: '4px 0 0 0' }}>{card.value?.toLocaleString()}</h3>
                        </div>
                    </motion.div>
                ))}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                {/* Top Flagged Users */}
                <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
                    <div style={{ padding: '20px 24px', borderBottom: '1px solid var(--panel-border)', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <Users size={18} color="var(--primary-color)" />
                        <h3 style={{ margin: 0, fontSize: '16px' }}>Top 5 Flagged Users</h3>
                    </div>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Flagged Count</th>
                            </tr>
                        </thead>
                        <tbody>
                            {topUsers.length === 0 ? (
                                <tr><td colSpan={3} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-secondary)' }}>No data</td></tr>
                            ) : topUsers.map((u: any) => (
                                <tr key={u.userId}>
                                    <td>{u.name}</td>
                                    <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{u.email}</td>
                                    <td><span className="badge badge-critical">{u.flaggedCount}</span></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* Recent Audit Logs */}
                <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
                    <div style={{ padding: '20px 24px', borderBottom: '1px solid var(--panel-border)' }}>
                        <h3 style={{ margin: 0, fontSize: '16px' }}>Recent Audit Logs</h3>
                    </div>
                    <div style={{ maxHeight: '340px', overflowY: 'auto' }}>
                        {auditLogs.length === 0 ? (
                            <p style={{ textAlign: 'center', padding: '24px', color: 'var(--text-secondary)' }}>No audit logs</p>
                        ) : auditLogs.map((log: any) => (
                            <div key={log.id} style={{ padding: '12px 24px', borderBottom: '1px solid var(--panel-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <p style={{ margin: 0, fontSize: '14px', fontWeight: 500 }}>{log.action}</p>
                                    <p style={{ margin: '2px 0 0 0', fontSize: '12px', color: 'var(--text-secondary)' }}>
                                        {log.entityType} #{log.entityId} • {log.user?.email || 'System'}
                                    </p>
                                </div>
                                <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-secondary)', flexShrink: 0, marginLeft: '12px' }}>
                                    {new Date(log.timestamp).toLocaleString()}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
