class AuthService {
  static TOKEN_KEY = 'auth_token';
  static USER_KEY = 'user_data';

  static getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  static setToken(token) {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  static getUser() {
    const userData = localStorage.getItem(this.USER_KEY);
    return userData ? JSON.parse(userData) : null;
  }

  static setUser(userData) {
    localStorage.setItem(this.USER_KEY, JSON.stringify(userData));
  }

  static logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  static isAuthenticated() {
    return !!this.getToken();
  }

  static getAuthHeaders() {
    const token = this.getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
}

export default AuthService;