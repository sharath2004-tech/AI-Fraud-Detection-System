import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { ShieldAlert, X } from 'lucide-react';

interface AlertToast {
    id: number;
    message: string;
}

const RealtimeToast: React.FC = () => {
    const [toasts, setToasts] = useState<AlertToast[]>([]);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = Stomp.over(socket);
        stompClient.debug = () => {}; // Disable debug logs

        stompClient.connect({}, () => {
            stompClient.subscribe('/topic/alerts', (message) => {
                const alertMessage = message.body || "New Critical Fraud Alert Detected!";
                const newToast = { id: Date.now(), message: alertMessage };
                
                setToasts(prev => [...prev, newToast]);

                // Auto remove after 5 seconds
                setTimeout(() => {
                    setToasts(prev => prev.filter(t => t.id !== newToast.id));
                }, 5000);
            });
        });

        return () => {
            if (stompClient.connected) {
                stompClient.disconnect(() => {});
            }
        };
    }, []);

    const removeToast = (id: number) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    };

    return (
        <div style={{ position: 'fixed', top: '24px', right: '24px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '12px', pointerEvents: 'none' }}>
            <AnimatePresence>
                {toasts.map(toast => (
                    <motion.div
                        key={toast.id}
                        initial={{ opacity: 0, x: 50, scale: 0.9 }}
                        animate={{ opacity: 1, x: 0, scale: 1 }}
                        exit={{ opacity: 0, x: 50, scale: 0.9 }}
                        style={{
                            background: 'rgba(15, 23, 42, 0.9)',
                            backdropFilter: 'blur(10px)',
                            border: '1px solid rgba(239, 68, 68, 0.3)',
                            borderLeft: '4px solid var(--danger-color)',
                            borderRadius: '8px',
                            padding: '16px',
                            width: '320px',
                            boxShadow: '0 10px 40px -10px rgba(239, 68, 68, 0.3)',
                            display: 'flex',
                            alignItems: 'flex-start',
                            gap: '12px',
                            pointerEvents: 'auto'
                        }}
                    >
                        <ShieldAlert color="var(--danger-color)" size={24} style={{ flexShrink: 0 }} />
                        <div style={{ flex: 1 }}>
                            <h4 style={{ margin: '0 0 4px 0', fontSize: '14px', color: '#fff' }}>Security Alert</h4>
                            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)' }}>{toast.message}</p>
                        </div>
                        <button 
                            onClick={() => removeToast(toast.id)}
                            style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', display: 'flex', padding: 0 }}
                        >
                            <X size={16} />
                        </button>
                    </motion.div>
                ))}
            </AnimatePresence>
        </div>
    );
};

export default RealtimeToast;
