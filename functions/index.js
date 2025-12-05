const { onRequest } = require("firebase-functions/v2/https");
const crypto = require("crypto");

const VNP_TMNCODE = "NPVI4W7D";
const VNP_HASH_SECRET = "VYLITKNPX0W90A4U5A6CBRASUBFI9LFO";
const VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

// BẮT BUỘC: phải là HTTPS trong sandbox
const RETURN_URL = "https://app-thue-xe.web.app/vnpay_return";

exports.createVNPayPayment = onRequest((req, res) => {
    try {
        const { amount, vehicleId } = req.body;

        if (!amount || !vehicleId) {
            return res.status(400).json({ error: "Thiếu amount hoặc vehicleId" });
        }

        const date = new Date();

        const createDate = date.toISOString().replace(/[-:T.Z]/g, "").substring(0, 14);

        const expireDateObj = new Date(date.getTime() + 15 * 60000);
        const expireDate = expireDateObj.toISOString().replace(/[-:T.Z]/g, "").substring(0, 14);

        let params = {
            vnp_Version: "2.1.0",
            vnp_Command: "pay",
            vnp_TmnCode: VNP_TMNCODE,
            vnp_Amount: amount * 100,
            vnp_CurrCode: "VND",
            vnp_TxnRef: Date.now().toString(),
            vnp_OrderInfo: `Thanh toan thue xe ${vehicleId}`,
            vnp_OrderType: "bill",
            vnp_ReturnUrl: RETURN_URL,
            vnp_IpAddr: "0.0.0.0",
            vnp_CreateDate: createDate,
            vnp_ExpireDate: expireDate,
            vnp_Locale: "vn",
        };

        // Sort keys
        const sortedKeys = Object.keys(params).sort();
        let rawData = "";

        sortedKeys.forEach((key, idx) => {
            rawData += key + "=" + params[key];
            if (idx < sortedKeys.length - 1) rawData += "&";
        });

        // Hash
        const secureHash = crypto
            .createHmac("sha512", VNP_HASH_SECRET)
            .update(rawData, "utf-8")
            .digest("hex");

        // Create final URL
        const query = sortedKeys
            .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
            .join("&");

        const payUrl = `${VNP_URL}?${query}&vnp_SecureHash=${secureHash}`;

        res.json({ payUrl });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});
