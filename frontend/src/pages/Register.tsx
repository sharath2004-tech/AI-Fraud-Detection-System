import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldAlert, Eye, EyeOff } from 'lucide-react';
import api from '../services/api';

const getPasswordStrength = (password: string): { label: string; color: string; width: string } => {
    if (password.length === 0) return { label: '', color: '#334155', width: '0%' };
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);
    const hasUpper = /[A-Z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const score = (password.length >= 8 ? 1 : 0) + (hasSpecial ? 1 : 0) + (hasUpper ? 1 : 0) + (hasNumber ? 1 : 0);
    if (score <= 1) return { label: 'Weak', color: '#ef4444', width: '33%' };
    if (score <= 3) return { label: 'Medium', color: '#eab308', width: '66%' };
    return { label: 'Strong', color: '#22c55e', width: '100%' };
};

const Register: React.FC = () => {
    const [form, setForm] = useState({ name: '', email: '', password: '', confirmPassword: '', role: 'USER' });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const strength = getPasswordStrength(form.password);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (form.name.trim().length < 2) return setError('Name must be at least 2 characters.');
        if (form.password !== form.confirmPassword) return setError('Passwords do not match.');
        if (strength.label === 'Weak') return setError('Please use a stronger password.');

        setLoading(true);
        try {
            await api.post('/auth/register', { name: form.name, email: form.email, password: form.password, role: 'USER' });
            navigate('/login', { state: { message: 'Registration successful! You can now log in.' } });
        } catch (err: any) {
            setError(err.response?.data?.message || 'Registration failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', width: '100vw', padding: '24px' }}>
            <div className="glass-panel" style={{ width: '100%', maxWidth: '440px', padding: '40px' }}>
                <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                    <ShieldAlert size={48} color="var(--primary-color)" style={{ marginBottom: '16px' }} />
                    <h1 style={{ fontSize: '24px', margin: 0 }}>Create Account</h1>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Join the NetGuard security platform</p>
                </div>

                {error && (
                    <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5', padding: '12px', borderRadius: '8px', marginBottom: '20px', border: '1px solid rgba(239, 68, 68, 0.2)', fontSize: '14px' }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">Full Name</label>
                        <input
                            type="text"
                            name="name"
                            className="form-control"
                            value={form.name}
                            onChange={handleChange}
                            placeholder="John Doe"
                            required
                            minLength={2}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Email Address</label>
                        <input
                            type="email"
                            name="email"
                            className="form-control"
                            value={form.email}
                            onChange={handleChange}
                            placeholder="john@example.com"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Password</label>
                        <div style={{ position: 'relative' }}>
                            <input
                                type={showPassword ? 'text' : 'password'}
                                name="password"
                                className="form-control"
                                value={form.password}
                                onChange={handleChange}
                                placeholder="Min. 8 characters"
                                required
                                style={{ paddingRight: '48px' }}
                            />
                            <button type="button" onClick={() => setShowPassword(p => !p)} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', display: 'flex', padding: 0 }}>
                                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                        {form.password.length > 0 && (
                            <div style={{ marginTop: '8px' }}>
                                <div style={{ height: '4px', borderRadius: '2px', background: '#334155', overflow: 'hidden' }}>
                                    <div style={{ height: '100%', width: strength.width, background: strength.color, transition: 'all 0.3s ease', borderRadius: '2px' }} />
                                </div>
                                <p style={{ fontSize: '12px', color: strength.color, marginTop: '4px' }}>{strength.label} password</p>
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <label className="form-label">Confirm Password</label>
                        <input
                            type="password"
                            name="confirmPassword"
                            className="form-control"
                            value={form.confirmPassword}
                            onChange={handleChange}
                            placeholder="Re-enter password"
                            required
                        />
                        {form.confirmPassword.length > 0 && form.password !== form.confirmPassword && (
                            <p style={{ fontSize: '12px', color: '#ef4444', marginTop: '4px' }}>Passwords do not match</p>
                        )}
                    </div>

                    <div className="form-group">
                        <label className="form-label">Role</label>
                        <select name="role" className="form-control" value={form.role} onChange={handleChange} disabled>
                            <option value="USER">User</option>
                        </select>
                        <p style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '4px' }}>Analysts and Admins are provisioned by administrators.</p>
                    </div>

                    <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '8px', padding: '12px' }} disabled={loading}>
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>

                    <div style={{ marginTop: '20px', textAlign: 'center' }}>
                        <Link to="/login" style={{ color: 'var(--primary-color)', fontSize: '14px', textDecoration: 'none' }}>
                            Already have an account? Sign In
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;
