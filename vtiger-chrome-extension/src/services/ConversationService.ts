import ConfigLoader from "../config/ConfigLoader";
import type { ApiSuccessResponse } from "../http/ApiSuccessResponse"
import axiosClient from "../http/axiosClient";

export type WhatsAppConversationMessage = {
    id: string;
    direction: string;
    author: string;
    date: string;
    messageText: string;
}

export type WhatsAppConversationFullInfo = {
    caseId: string;
    caseNo: string;
    msisdn: string;
    contactName: string;
    messages: WhatsAppConversationMessage[];
    status: WhatsAppConversationStatus;
    closedBy?: string;
    dateClosed?: Date;
}

export type WhatsAppConversationStatus = 'OPEN' | "CLOSED";

const getConversationForCase = (caseId: string): Promise<ApiSuccessResponse<WhatsAppConversationFullInfo | undefined>> => {
    return axiosClient.get(`${ConfigLoader.getConfig().apiBaseUrl}/vtiger/conversations/case/${caseId}`);
}

const sendMessage = (caseId: string, message: string): Promise<ApiSuccessResponse<WhatsAppConversationFullInfo>> => {
    return axiosClient.post(`${ConfigLoader.getConfig().apiBaseUrl}/vtiger/conversations/case/${caseId}/messages/send`, { caseId, message });
}

const closeConverstation = (caseId: string): Promise<ApiSuccessResponse<WhatsAppConversationFullInfo>> => {
    return axiosClient.post(`${ConfigLoader.getConfig().apiBaseUrl}/vtiger/conversations/case/${caseId}/close`, { caseId });
}

const tryAuth = (username: string, accessKey: string): Promise<ApiSuccessResponse<{ token: string }>> => {
    return axiosClient.post(`${ConfigLoader.getConfig().apiBaseUrl}/vtiger/tryauth`, { username, accessKey });
}

export default {
    getConversationForCase,
    sendMessage,
    closeConverstation,
    tryAuth
}