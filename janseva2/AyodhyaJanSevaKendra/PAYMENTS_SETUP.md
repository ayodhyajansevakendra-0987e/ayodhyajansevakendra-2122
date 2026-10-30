# Payment integration – not enabled yet

No payment provider was selected and no merchant credentials were supplied. Therefore this app must not mark any payment successful or generate a paid receipt yet.

Before enabling payments:
1. Owner chooses a regulated payment gateway/provider and opens a merchant account.
2. Configure test and live keys only in a trusted backend secret store (never in Android or the React app).
3. Backend creates an order/payment intent and stores it as `pending`.
4. Customer completes payment through the provider's approved checkout flow or supported UPI intent.
5. Provider sends a signed webhook to a server endpoint. Verify the signature, amount, currency, merchant/order reference and idempotency before changing status to `verified`.
6. Generate receipt only from the server after verification; store receipt number, provider reference, amount, date, and status.
7. Test success, failure, cancellation, duplicate webhook, delayed webhook, refund and mismatched amount cases.

Never ask customers for UPI PIN, OTP, card PIN or banking password. Never accept a screenshot as automatic payment proof. Cash at shop should be recorded by an authorized admin and audited.
