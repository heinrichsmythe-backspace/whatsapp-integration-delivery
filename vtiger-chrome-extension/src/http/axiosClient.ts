import axios, { type RawAxiosRequestHeaders } from 'axios';

const instance = axios.create();

instance.interceptors.response.use((response) => {
  return response;
}, (error) => {
  if(error?.response?.status == 401){
    onLogout();
  }
  return Promise.reject(error);
});

const onLogout = () => {
  removeToken();
}

export const setToken = (token: string) => {
  instance.defaults.headers.common['X-Auth-Token'] = `${token}`;
}

const removeToken = () => {
  instance.defaults.headers.common['X-Auth-Token'] = ``;
}

const get = async <T>(url: string): Promise<T> => {
  const res = await instance.get(url);
  return res.data as T;
}

const post = async <T>(url: string, payload: any, headers?: RawAxiosRequestHeaders): Promise<T> => {
  const res = await instance.post(url, payload, { headers });
  return res.data as T;
}

const put = async <T>(url: string, payload: any): Promise<T> => {
  const res = await instance.put(url, payload);
  return res.data as T;
}

const doDelete = async <T>(url: string, payload: any): Promise<T> => {
  const res = await instance.delete(url, { data: payload });
  return res.data as T;
}

const post_withBlobResponse = async (url: string, payload: any, headers?: RawAxiosRequestHeaders): Promise<any> => {
  const res = await instance.post(url, payload, { responseType: 'blob', headers });
  return res;
}

const get_withBlobResponse = async (url: string): Promise<any> => {
  const res = await instance.get(url, { responseType: 'blob' });
  return res;
}

export default {
  get,
  post,
  put,
  doDelete,
  post_withBlobResponse,
  get_withBlobResponse,
  onLogout
};