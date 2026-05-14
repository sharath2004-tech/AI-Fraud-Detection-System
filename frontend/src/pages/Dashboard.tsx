import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Activity, AlertTriangle, ShieldCheck, CreditCard } from 'lucide-react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell, Legend
} from 'recharts';
import api from '../services/api';

interface DashboardMetrics {
    totalTransactions: number;
    flaggedTransactions: number;
    totalAlerts: number;
    unresolvedAlerts: number;
    criticalAlerts: number;
}

interface DailyData {
    date: string;
    totalTransactions: number;
    flaggedTransactions: number;
}

interface RiskDist {
    [key: string]: number;
}

const SEVERITY_COLORS: Record<string, string> = {
    LOW: '#22c55e',
    MEDIUM: '#eab308',
    HIGH: '#f97316',
    CRITICAL: '#ef4444',
};

const StatCard = ({ title, value, icon, color, delay }: any) => (
    <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay, duration: 0.4 }}
        className="glass-panel"
        style={{ flex: 1, minWidth: '200px', display: 'flex', alignItems: 'center', gap: '20px' }}
    >
        <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: `var(--${color}-color)`, opacity: 0.8, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff' }}>
            {icon}
        </div>
        <div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.5px', margin: 0 }}>{title}</p>
            <h3 style={{ fontSize: '28px', margin: '4px 0 0 0' }}>{value?.toLocaleString()}</h3>
        </div>
    </motion.div>
);

const Dashboard: React.FC = () => {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [dailyData, setDailyData] = useState<DailyData[]>([]);
    const [riskData, setRiskData] = useState<{ name: string; value: number }[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAll = async () => {
            try {
                const [metricsRes, dailyRes, riskRes] = await Promise.all([
                    api.get('/dashboard/metrics'),
                    api.get('/dashboard/daily'),
                    api.get('/dashboard/risk-distribution'),
                ]);

                setMetrics(metricsRes.data.data);

                const daily: DailyData[] = (dailyRes.data.data || []).map((d: any) => ({
                    date: new Date(d.date).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' }),
                    totalTransactions: d.totalTransactions,
                    flaggedTransactions: d.flaggedTransactions,
                }));
                setDailyData(daily);

                const risk: RiskDist = riskRes.data.data || {};
                setRiskData(
                    Object.entries(risk).map(([name, value]) => ({ name, value: value as number }))
                );
            } catch (error) {
                console.error('Failed to fetch dashboard data', error);
            } finally {
                setLoading(false);
            }
        };
        fetchAll();
    }, []);

    if (loading || !metrics) {
        return (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', flexDirection: 'column', gap: '16px' }}>
                <div style={{ width: '40px', height: '40px', border: '3px solid var(--primary-color)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
                <p style={{ color: 'var(--text-secondary)' }}>Loading dashboard...</p>
            </div>
        );
    }

    return (
        <div>
            <div style={{ marginBottom: '32px' }}>
                <h1 style={{ margin: 0 }}>Command Center</h1>
                <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Real-time overview of system security metrics.</p>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px', marginBottom: '32px' }}>
                <StatCard title="Total Transactions" value={metrics.totalTransactions} icon={<CreditCard />} color="primary" delay={0.1} />
                <StatCard title="Flagged (Suspicious)" value={metrics.flaggedTransactions} icon={<Activity />} color="warning" delay={0.2} />
                <StatCard title="Unresolved Alerts" value={metrics.unresolvedAlerts} icon={<AlertTriangle />} color="danger" delay={0.3} />
                <StatCard title="Total Security Alerts" value={metrics.totalAlerts} icon={<ShieldCheck />} color="success" delay={0.4} />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) minmax(0, 1fr)', gap: '24px', marginBottom: '24px' }}>
                {/* Bar Chart */}
                <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', minHeight: '380px' }}>
                    <h3 style={{ marginBottom: '8px', margin: '0 0 4px 0' }}>Transaction Volume</h3>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '24px', margin: '0 0 24px 0' }}>Last 30 days — total vs flagged</p>
                    <div style={{ flex: 1, width: '100%', minHeight: 0 }}>
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={dailyData} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                                <XAxis dataKey="date" stroke="var(--text-secondary)" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
                                <YAxis stroke="var(--text-secondary)" tick={{ fontSize: 11 }} />
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#0f172a', border: '1px solid var(--panel-border)', borderRadius: '8px' }}
                                    itemStyle={{ color: '#fff' }}
                                />
                                <Bar dataKey="totalTransactions" name="Total" fill="var(--primary-color)" radius={[4, 4, 0, 0]} />
                                <Bar dataKey="flaggedTransactions" name="Flagged" fill="var(--danger-color)" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Pie Chart */}
                <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', minHeight: '380px' }}>
                    <h3 style={{ margin: '0 0 4px 0' }}>Risk Distribution</h3>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '13px', margin: '0 0 24px 0' }}>Alert severity breakdown</p>
                    <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <ResponsiveContainer width="100%" height={280}>
                            <PieChart>
                                <Pie
                                    data={riskData}
                                    cx="50%"
                                    cy="45%"
                                    innerRadius={65}
                                    outerRadius={100}
                                    paddingAngle={3}
                                    dataKey="value"
                                >
                                    {riskData.map((entry) => (
                                        <Cell key={entry.name} fill={SEVERITY_COLORS[entry.name] || '#64748b'} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#0f172a', border: '1px solid var(--panel-border)', borderRadius: '8px' }}
                                    itemStyle={{ color: '#fff' }}
                                />
                                <Legend
                                    iconType="circle"
                                    formatter={(value) => <span style={{ color: '#cbd5e1', fontSize: '12px' }}>{value}</span>}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
