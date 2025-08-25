const handleApiErrorResponse = async (data: any): Promise<void> => {
    console.error(data);
    console.error(data.response);
    let friendlyError = 'Unknown Error';
    if (data.response && data.response.data && typeof data.response.data === 'string' && data.response.data.length > 0) {
        friendlyError = data.response.data;
    }

    if (data.message) {
        friendlyError = data.message;
    }

    if (data && data.response && data.response.data) {
        try {
            if (data.response.data.message && typeof data.response.data.message === 'string' && data.response.data.message.length > 0) {
                friendlyError = data.response.data.message;
            }
            if (data.response.data instanceof Blob) {
                const jsonBlob = data.response.data;
                const blobText = await jsonBlob.text();
                // console.log(blobText)
                // const blobJson = JSON.parse(blobText);
                friendlyError = blobText;
            }
            if (data.response.data && typeof data.response.data === 'string' && data.response.data > 0) {
                friendlyError = data.response.data;
            }
        } catch (e) {
            console.error(e)
        }
    }

    alert(`Error: ${friendlyError}`);
}

const generalError = (errorMessage: string) => {
    alert(`Error: ${errorMessage}`);
}

export default {
    handleApiErrorResponse,
    generalError
}