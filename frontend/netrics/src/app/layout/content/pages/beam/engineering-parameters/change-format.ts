import {UserDto} from '../../../../../models/pulse/UserDto';

/** Shared by the site sheet and the landing page's change feed */
export const RECENT_MS = 24 * 3600 * 1000;
export const SYSTEM_USERS = ['SYSTEM', 'ANONYMOUS'];
const relativeFormat = new Intl.RelativeTimeFormat('en', {numeric: 'auto'});

export function relativeTime(date: Date): string {
  const ago = (Date.now() - date.getTime()) / 1000;
  if (ago < 60) return 'just now';
  if (ago < 3600) return relativeFormat.format(-Math.round(ago / 60), 'minute');
  if (ago < 86400) return relativeFormat.format(-Math.round(ago / 3600), 'hour');
  if (ago < 86400 * 30) return relativeFormat.format(-Math.round(ago / 86400), 'day');
  return 'on ' + date.toLocaleDateString(undefined, {dateStyle: 'medium'});
}

export function absoluteTime(date: Date): string {
  return date.toLocaleString(undefined, {dateStyle: 'medium', timeStyle: 'short'});
}

/** Keycloak sub -> "First L." */
export function userNameMap(users: UserDto[]): Map<string, string> {
  return new Map(users.map(u => [u.userId,
    [u.firstName, u.lastName?.charAt(0) ? u.lastName.charAt(0) + '.' : ''].filter(Boolean).join(' ') || u.username]));
}

/** Sites opened on the site sheet, newest first; per browser only */
const RECENT_SITES_KEY = 'beam.ep.recentSites';
const RECENT_SITES_MAX = 8;

export function recentSites(): string[] {
  try {
    const list = JSON.parse(localStorage.getItem(RECENT_SITES_KEY) ?? '[]');
    return Array.isArray(list) ? list.filter(c => typeof c === 'string') : [];
  } catch {
    return [];
  }
}

export function rememberSite(siteCode: string): void {
  try {
    const list = [siteCode, ...recentSites().filter(c => c !== siteCode)].slice(0, RECENT_SITES_MAX);
    localStorage.setItem(RECENT_SITES_KEY, JSON.stringify(list));
  } catch {
    // storage blocked: the list just stays empty
  }
}
