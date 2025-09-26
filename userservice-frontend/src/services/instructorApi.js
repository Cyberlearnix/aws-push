// src/services/instructorApi.js
// All instructor-service APIs via API Gateway (base '/api')
// Reuses token from localStorage (added by login flow)

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || '/api';

async function request(path, { method = 'GET', body, headers = {} } = {}) {
  const token = localStorage.getItem('accessToken');
  const url = `${API_BASE_URL}${path}`;
  const init = {
    method,
    headers: {
      ...(body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body ? (body instanceof FormData ? body : JSON.stringify(body)) : undefined,
  };
  const res = await fetch(url, init);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('application/json')) return res.json();
  return res.text();
}

// Dashboard
export const getDashboard = (instructorId) => request(`/instructors/${instructorId}/dashboard`);
export const getCourseAnalytics = (instructorId, courseId) => request(`/instructors/${instructorId}/courses/${courseId}/analytics`);
export const getEarnings = (instructorId) => request(`/instructors/${instructorId}/earnings`);

// Courses
export const createCourse = (instructorId, body) => request(`/instructors/${instructorId}/courses`, { method: 'POST', body });
export const listCourses = (instructorId) => request(`/instructors/${instructorId}/courses`);
export const getCourse = (instructorId, courseId) => request(`/instructors/${instructorId}/courses/${courseId}`);
export const updateCourse = (instructorId, courseId, body) => request(`/instructors/${instructorId}/courses/${courseId}`, { method: 'PUT', body });
export const deleteCourse = (instructorId, courseId) => request(`/instructors/${instructorId}/courses/${courseId}`, { method: 'DELETE' });

// Content (Modules) & Upload
export const addModule = (instructorId, courseId, body) => request(`/instructors/${instructorId}/courses/${courseId}/modules`, { method: 'POST', body });
export const updateModule = (instructorId, courseId, moduleId, body) => request(`/instructors/${instructorId}/courses/${courseId}/modules/${moduleId}`, { method: 'PUT', body });
export const deleteModule = (instructorId, courseId, moduleId) => request(`/instructors/${instructorId}/courses/${courseId}/modules/${moduleId}`, { method: 'DELETE' });
export const uploadResource = (instructorId, courseId, file) => {
  const form = new FormData();
  form.append('file', file);
  return request(`/instructors/${instructorId}/courses/${courseId}/upload`, { method: 'POST', body: form });
};

// Communication
export const postAnnouncement = (instructorId, courseId, body) => request(`/instructors/${instructorId}/courses/${courseId}/announcements`, { method: 'POST', body });
export const sendMessage = (instructorId, courseId, body) => request(`/instructors/${instructorId}/courses/${courseId}/messages`, { method: 'POST', body });
export const listAnnouncements = (instructorId, courseId) => request(`/instructors/${instructorId}/courses/${courseId}/announcements`);
export const listMessages = (instructorId, courseId) => request(`/instructors/${instructorId}/courses/${courseId}/messages`);

// Students & Grades
export const listStudents = (instructorId, courseId) => request(`/instructors/${instructorId}/courses/${courseId}/students`);
export const getStudentProgress = (instructorId, courseId, studentId) => request(`/instructors/${instructorId}/courses/${courseId}/students/${studentId}`);
export const setGrade = (instructorId, courseId, body) => request(`/instructors/${instructorId}/courses/${courseId}/grades`, { method: 'POST', body });

export default {
  getDashboard,
  getCourseAnalytics,
  getEarnings,
  createCourse,
  listCourses,
  getCourse,
  updateCourse,
  deleteCourse,
  addModule,
  updateModule,
  deleteModule,
  uploadResource,
  postAnnouncement,
  sendMessage,
  listAnnouncements,
  listMessages,
  listStudents,
  getStudentProgress,
  setGrade,
};
