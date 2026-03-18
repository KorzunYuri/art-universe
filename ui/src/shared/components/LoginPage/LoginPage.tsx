import { GoogleLogin, type CredentialResponse } from '@react-oauth/google';
import { useState } from 'react';
import { useAuth } from '@/shared/hooks/useAuth';
import styles from './LoginPage.module.css';

export function LoginPage() {
    const { login } = useAuth();
    const [error, setError] = useState<string | null>(null);

    const handleGoogleSuccess = async (response: CredentialResponse) => {
        setError(null);
        if (!response.credential) {
            setError('No credential received from Google');
            return;
        }
        try {
            await login(response.credential);
        } catch {
            setError('Login failed. Please try again.');
        }
    };

    const handleGoogleError = () => {
        setError('Google Sign-In failed. Please try again.');
    };

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h1 className={styles.title}>Art Universe</h1>
                <p className={styles.subtitle}>Sign in to continue</p>
                <div className={styles.googleButton}>
                    <GoogleLogin
                        onSuccess={handleGoogleSuccess}
                        onError={handleGoogleError}
                        size="large"
                        theme="outline"
                        text="signin_with"
                        shape="rectangular"
                        width="280"
                    />
                </div>
                {error && <p className={styles.error}>{error}</p>}
            </div>
        </div>
    );
}
