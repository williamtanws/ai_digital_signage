/**
 * API Service for Dashboard Data
 * 
 * Handles communication with the backend Spring Boot service.
 * Fetches dashboard overview data from a single endpoint.
 */

import axios from 'axios';

// API base URL - configured to work with Vite proxy
const API_BASE_URL = '/api';

/**
 * Dashboard API Service
 */
export const dashboardApi = {
  /**
   * Fetch complete dashboard overview data
   * 
   * Makes a single GET request to retrieve all dashboard metrics:
   * - Top-level KPIs (audience, views, ads, avg view time)
   * - Age distribution
   * - Gender distribution
   * - Emotion distribution
   * - Advertisement performance
   * - Advertisement attention metrics
   * 
   * @returns {Promise} Promise resolving to dashboard data object
   * @throws {Error} If the API request fails
   */
  async getDashboardOverview() {
    try {
      const response = await axios.get(`${API_BASE_URL}/dashboard/overview`);
      return response.data;
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      throw new Error(
        error.response?.data?.message || 
        'Failed to fetch dashboard data. Please ensure the backend service is running.'
      );
    }
  }
};

export default dashboardApi;
