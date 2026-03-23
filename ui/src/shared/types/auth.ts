export type Role =
    | 'VIEWER'
    | 'LASTFM_CURATOR'
    | 'SPOTIFY_CURATOR'
    | 'MASTER_CURATOR'
    | 'QUIZ_MASTER'
    | 'CONFIG_MANAGER'
    | 'MAINTAINER'
    | 'SURVEYOR'
    | 'ADMIN';

export interface UserInfo {
    id: number;
    email: string;
    name: string;
    pictureUrl: string | null;
    roles: Role[];
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    user: UserInfo;
}
