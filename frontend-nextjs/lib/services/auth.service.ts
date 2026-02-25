import axios from 'axios';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserInfo {
  username: string;
  role: string;
  countyId?: string;
  districtId?: string;
  preferred_username?: string;
  name?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  user: UserInfo;
}

/**
 * Parse user info from JWT token
 */
function parseUserInfoFromToken(token: string): UserInfo {
  try {
    const parts = token.split('.');
    if (parts.length < 2) {
      throw new Error('Invalid token format');
    }

    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));

    const userInfo: UserInfo = {
      username: payload.preferred_username || payload.username || '',
      role: '',
      preferred_username: payload.preferred_username,
      name: payload.name,
    };

    // Extract role from client roles (cmips-frontend) or realm roles
    // Priority: ADMIN > other roles
    let allValidRoles: string[] = [];

    // Debug: Log raw payload structure
    if (typeof window !== 'undefined') {
      console.log('🔍 JWT Payload structure:', {
        hasResourceAccess: !!payload.resource_access,
        resourceAccessKeys: payload.resource_access ? Object.keys(payload.resource_access) : [],
        hasCmipsApp: !!payload.resource_access?.['cmips-frontend'],
        cmipsAppKeys: payload.resource_access?.['cmips-frontend'] ? Object.keys(payload.resource_access['cmips-frontend']) : [],
        clientRoles: payload.resource_access?.['cmips-frontend']?.roles,
        hasRealmAccess: !!payload.realm_access,
        realmRoles: payload.realm_access?.roles,
        allClaims: Object.keys(payload)
      });
    }

    if (payload.resource_access?.['cmips-frontend']?.roles) {
      const clientRoles = payload.resource_access['cmips-frontend'].roles;
      console.log('🔍 Raw client roles:', clientRoles);
      // Filter out null, default roles, and system roles
      const validClientRoles = clientRoles.filter((role: string) =>
        role &&
        typeof role === 'string' &&
        role.trim() !== '' &&
        !role.startsWith('default-roles-') &&
        role !== 'offline_access' &&
        role !== 'uma_authorization'
      );
      console.log('🔍 Valid client roles after filtering:', validClientRoles);
      allValidRoles = [...allValidRoles, ...validClientRoles];
    }

    if (payload.realm_access?.roles) {
      const realmRoles = payload.realm_access.roles;
      console.log('🔍 Raw realm roles:', realmRoles);
      const validRealmRoles = realmRoles.filter((role: string) =>
        role &&
        typeof role === 'string' &&
        role.trim() !== '' &&
        !role.startsWith('default-roles-') &&
        role !== 'offline_access' &&
        role !== 'uma_authorization'
      );
      console.log('🔍 Valid realm roles after filtering:', validRealmRoles);
      allValidRoles = [...allValidRoles, ...validRealmRoles];
    }

    console.log('🔍 All valid roles before deduplication:', allValidRoles);

    // Remove duplicates and normalize to uppercase, but keep original case for final assignment
    const uniqueRoles = Array.from(new Set(allValidRoles.map(r => r.toUpperCase())));
    const originalRoles = Array.from(new Set(allValidRoles));

    console.log('🔍 Unique roles (uppercase):', uniqueRoles);
    console.log('🔍 Original roles (preserved case):', originalRoles);

    // Prioritize ADMIN, then use first valid role
    if (uniqueRoles.includes('ADMIN')) {
      const adminRole = originalRoles.find(r => r.toUpperCase() === 'ADMIN') || 'ADMIN';
      userInfo.role = adminRole;
      console.log('✅ Selected ADMIN role:', userInfo.role);
    } else if (uniqueRoles.length > 0) {
      // Use first role, preserving original case but normalize to expected format
      const selectedRole = originalRoles[0];
      // Normalize role names to match backend expectations
      const normalizedRole = selectedRole.toUpperCase();
      userInfo.role = normalizedRole;
      console.log('✅ Selected first valid role:', userInfo.role, '(from:', selectedRole, ')');
    } else {
      // CRITICAL: No roles found - try to extract from other possible locations
      console.error('❌ No valid roles found after filtering!', {
        allValidRoles,
        uniqueRoles,
        clientRoles: payload.resource_access?.['cmips-frontend']?.roles,
        realmRoles: payload.realm_access?.roles,
        allResourceAccess: payload.resource_access,
        allRealmAccess: payload.realm_access
      });

      // Try to find roles in ANY client (not just cmips-frontend)
      if (payload.resource_access) {
        for (const [clientName, clientData] of Object.entries(payload.resource_access)) {
          const client = clientData as any;
          if (client?.roles && Array.isArray(client.roles)) {
            const foundRoles = client.roles.filter((r: string) =>
              r && typeof r === 'string' && r.trim() !== '' &&
              !r.startsWith('default-roles-') &&
              r !== 'offline_access' &&
              r !== 'uma_authorization'
            );
            if (foundRoles.length > 0) {
              console.warn(`⚠️ Found roles in client '${clientName}':`, foundRoles);
              userInfo.role = foundRoles[0].toUpperCase();
              console.log('✅ Using role from client', clientName, ':', userInfo.role);
              break;
            }
          }
        }
      }

      // If still no role, set to empty and log error
      if (!userInfo.role || userInfo.role === '') {
        console.error('❌ CRITICAL: Could not extract role from JWT token!');
        console.error('❌ Full payload for debugging:', JSON.stringify(payload, null, 2));
        userInfo.role = ''; // Keep as empty string to indicate failure
      }
    }

    // Debug logging
    if (typeof window !== 'undefined') {
      console.log('🔍 Final role extraction result:', {
        clientRoles: payload.resource_access?.['cmips-frontend']?.roles,
        realmRoles: payload.realm_access?.roles,
        allValidRoles: allValidRoles,
        uniqueRoles: uniqueRoles,
        selectedRole: userInfo.role,
        userInfo: userInfo
      });
    }

    // Extract countyId if present (check both direct claim and attributes)
    // Support both camelCase (countyId) and snake_case (county_id) formats
    // Also support county codes from groups (CTA, CTB, CTC, CT1-CT5)
    console.log('🔍 Extracting countyId from JWT payload:', {
      hasCountyId: !!payload.countyId,
      countyId: payload.countyId,
      hasCounty_id: !!payload.county_id,
      county_id: payload.county_id,
      hasAttributes: !!payload.attributes,
      attributesKeys: payload.attributes ? Object.keys(payload.attributes) : [],
      attributesCountyId: payload.attributes?.countyId,
      attributesCounty_id: payload.attributes?.county_id,
      groups: payload.groups
    });

    if (payload.countyId) {
      userInfo.countyId = payload.countyId;
      console.log('✅ Found countyId directly:', userInfo.countyId);
    } else if (payload.county_id) {
      userInfo.countyId = payload.county_id;
      console.log('✅ Found county_id directly:', userInfo.countyId);
    } else if (payload.attributes?.countyId) {
      // Handle array or single value
      const countyIdValue = Array.isArray(payload.attributes.countyId)
        ? payload.attributes.countyId[0]
        : payload.attributes.countyId;
      if (countyIdValue) {
        userInfo.countyId = countyIdValue;
        console.log('✅ Found countyId in attributes:', userInfo.countyId);
      }
    } else if (payload.attributes?.county_id) {
      // Handle array or single value for snake_case
      const countyIdValue = Array.isArray(payload.attributes.county_id)
        ? payload.attributes.county_id[0]
        : payload.attributes.county_id;
      if (countyIdValue) {
        userInfo.countyId = countyIdValue;
        console.log('✅ Found county_id in attributes:', userInfo.countyId);
      }
    } else if (payload.groups && Array.isArray(payload.groups)) {
      // Try to extract county from groups (CTA, CTB, CTC, CT1-CT5)
      const countyGroup = payload.groups.find((g: string) =>
        /^(CTA|CTB|CTC|CT[1-5])$/i.test(g) ||
        /\/(CTA|CTB|CTC|CT[1-5])$/i.test(g)
      );
      if (countyGroup) {
        const match = countyGroup.match(/(CTA|CTB|CTC|CT[1-5])$/i);
        if (match) {
          userInfo.countyId = match[1].toUpperCase();
          console.log('✅ Found countyId in groups:', userInfo.countyId);
        }
      }
    } else {
      console.warn('⚠️ No countyId found in JWT token payload');
    }

    // Extract districtId if present
    if (payload.districtId) {
      userInfo.districtId = payload.districtId;
    } else if (payload.attributes?.districtId) {
      const districtIdValue = Array.isArray(payload.attributes.districtId)
        ? payload.attributes.districtId[0]
        : payload.attributes.districtId;
      if (districtIdValue) {
        userInfo.districtId = districtIdValue;
      }
    }

    // Final validation - ensure role is set
    if (!userInfo.role || userInfo.role.trim() === '') {
      console.error('❌ CRITICAL ERROR: Role extraction failed - userInfo.role is empty!');
      console.error('❌ UserInfo object:', userInfo);
      console.error('❌ This will cause "User role missing from session" error');
    } else {
      console.log('✅ SUCCESS: Role extracted successfully:', userInfo.role);
    }

    console.log('✅ Final userInfo:', {
      username: userInfo.username,
      role: userInfo.role,
      countyId: userInfo.countyId,
      districtId: userInfo.districtId
    });

    return userInfo;
  } catch (error) {
    console.error('❌ Error parsing JWT token:', error);
    console.error('❌ Error stack:', error instanceof Error ? error.stack : 'No stack trace');
    return {
      username: '',
      role: '',
    };
  }
}

export const authService = {
  /**
   * Login with username and password - calls API Gateway
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    // Use API Gateway for authentication
    const gatewayUrl = typeof window !== 'undefined'
      ? (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090')
      : (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090');
    const loginUrl = `${gatewayUrl}/api/auth/login`;

    try {
      // Send JSON request to gateway
      const response = await axios.post(loginUrl, {
        username: credentials.username,
        password: credentials.password,
      }, {
        headers: {
          'Content-Type': 'application/json'
        },
        withCredentials: true
      });

      const accessToken = response.data.access_token;
      const refreshToken = response.data.refresh_token;

      if (!accessToken) {
        throw new Error('No access token received from gateway');
      }

      // Store tokens in localStorage
      if (typeof window !== 'undefined') {
        localStorage.setItem('token', accessToken);
        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }
      }

      // Parse user info from JWT token
      const userInfo = parseUserInfoFromToken(accessToken);

      return {
        accessToken,
        refreshToken,
        user: userInfo,
      };
    } catch (error: any) {
      if (axios.isAxiosError(error)) {
        console.error('Login error details:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          message: error.message,
          code: error.code,
        });

        const message = error.response?.data?.error_description ||
                       error.response?.data?.error ||
                       'Login failed. Please check your credentials.';
        throw new Error(message);
      }
      throw error;
    }
  },

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    try {
      const gatewayUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090';
      const refreshToken = typeof window !== 'undefined' ? localStorage.getItem('refreshToken') : null;
      await axios.post(`${gatewayUrl}/api/auth/logout`, { refresh_token: refreshToken });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
      }
    }
  },

  /**
   * Get user info from JWT token stored in localStorage
   */
  async getUserInfo(): Promise<UserInfo> {
    const token = this.getToken();
    if (!token) {
      throw new Error('No authentication token found');
    }

    // Parse user info directly from JWT token
    return parseUserInfoFromToken(token);
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    if (typeof window === 'undefined') return false;
    const token = localStorage.getItem('token');
    if (!token) return false;

    // Check if token is expired
    try {
      const parts = token.split('.');
      if (parts.length < 2) return false;
      const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
      return payload.exp > Math.floor(Date.now() / 1000);
    } catch {
      return false;
    }
  },

  /**
   * Get stored auth token
   */
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('token');
  },
};
