import React from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LayoutDashboard, AlertCircle, FileText, Users, ShieldOff, BarChart3, LogOut, ShieldAlert } from 'lucide-react';

const Layout: React.FC = () => {
    const { user, logout } = useAuth();
    const location = useLocation();

    const baseNav = [
        { path: '/dashboard', label: 'Command Center', icon: <LayoutDashboard size={20} /> },
        { path: '/transactions', label: 'Transactions', icon: <FileText size={20} /> },
    ];

    const analystNav = [
        { path: '/alerts', label: 'Fraud Alerts', icon: <AlertCircle size={20} /> },
    ];

    const adminNav = [
        { path: '/admin/dashboard', label: 'Admin Overview', icon: <BarChart3 size={20} /> },
        { path: '/admin/users', label: 'User Management', icon: <Users size={20} /> },
        { path: '/admin/blacklist', label: 'Blacklist', icon: <ShieldOff size={20} /> },
    ];

    const navItems = [
        ...baseNav,
        ...(user?.role === 'ANALYST' || user?.role === 'ADMIN' ? analystNav : []),
        ...(user?.role === 'ADMIN' ? adminNav : []),
    ];

    return (
        <div className="app-container">
            <aside className="sidebar">
                <div style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: '1px solid var(--panel-border)' }}>
                    <ShieldAlert size={28} color="var(--primary-color)" />
                    <h2 style={{ fontSize: '18px', margin: 0 }}>NetGuard</h2>
                </div>
                
                <div style={{ padding: '20px 16px', flex: 1 }}>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '16px', paddingLeft: '8px' }}>Menu</p>
                    <nav style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {navItems.map(item => {
                            const isActive = location.pathname.startsWith(item.path);
                            return (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '12px',
                                        padding: '12px 16px',
                                        borderRadius: '8px',
                                        color: isActive ? '#fff' : 'var(--text-secondary)',
                                        background: isActive ? 'rgba(59, 130, 246, 0.15)' : 'transparent',
                                        borderLeft: isActive ? '3px solid var(--primary-color)' : '3px solid transparent',
                                        transition: 'all 0.2s',
                                        textDecoration: 'none',
                                        fontWeight: isActive ? 500 : 400
                                    }}
                                >
                                    {item.icon}
                                    {item.label}
                                </Link>
                            )
                        })}
                    </nav>
                </div>

                <div style={{ padding: '20px 16px', borderTop: '1px solid var(--panel-border)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px', paddingLeft: '8px' }}>
                        <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'var(--primary-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 'bold' }}>
                            {user?.name?.charAt(0)}
                        </div>
                        <div>
                            <p style={{ fontSize: '14px', fontWeight: 500, margin: 0, color: '#fff' }}>{user?.name}</p>
                            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: 0 }}>{user?.role}</p>
                        </div>
                    </div>
                    <button 
                        onClick={logout}
                        style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'none', border: 'none', color: 'var(--danger-color)', cursor: 'pointer', padding: '8px', width: '100%', textAlign: 'left', fontSize: '14px', fontWeight: 500 }}
                    >
                        <LogOut size={16} /> Logout
                    </button>
                </div>
            </aside>

            <main className="main-content">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
