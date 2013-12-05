package de.whs.studmap.client.core.snippets;

import android.content.Context;
import de.whs.studmap.client.core.web.ResponseError;

public class ErrorHandler implements ResponseError {

	private Context mContext;

	public ErrorHandler(Context ctx) {
		mContext = ctx;
	}

	public void handle(int error) {
		switch (error) {
		case TaskCancelled:
			onTaskCancelled();
			break;
			
		case ConnectionError:
			onConnectionError();
			break;

		case None:
			onNoError();
			break;

		case DatabaseError:
			onDatabaseError();
			break;

		case UnknownError:
			onUnknownError();
			break;

		case UserNameDuplicate:
			onUserNameDuplicate();
			break;

		case UserNameInvalid:
			onUserNameInvalid();
			break;

		case PasswordInvalid:
			onPasswordInvalid();
			break;

		case LoginInvalid:
			onLoginInvalid();
			break;

		case MapIdDoesNotExist:
			onMapIdDoesNotExist();
			break;

		case FloorIdDoesNotExist:
			onFloorIdDoesNotExist();
			break;

		case NodeIdDoesNotExist:
			onNodeIdDoesNotExist();
			break;

		case NoRouteFound:
			onNoRouteFound();
			break;

		case StartNodeNotFound:
			onStartNodeNotFound();
			break;

		case EndNodeNotFound:
			onEndNodeNotFound();
			break;

		case PoiTypeIdDoesNotExist:
			onPoiTypeIdDoesNotExist();
			break;

		case NFCTagDoesNotExist:
			onNFCTagDoesNotExist();
			break;

		case QRCodeDosNotExist:
			onQRCodeDoesNotExist();
			break;

		case PoiDoesNotExist:
			onPoiDoesNotExist();
			break;

		case QRCodeIsNullOrEmpty:
			onQRCodeIsNullOrEmtpy();
			break;

		case NFCTagIsNullOrEmpty:
			onNFCTagIsNullOrEmpty();
			break;

		case NFCTagAllreadyAssigned:
			onNFCTagAllreadyAssigned();
			break;

		case FingeprintIsNotDefined:
			onFingerprintIsNotDefined();
			break;

		default:
			break;
		}
	}

	private void onTaskCancelled() {
	UserInfo.toast(mContext, "Das Laden wurde abgebrochen!", false);	
	}

	private void onFingerprintIsNotDefined() {
		UserInfo.toast(mContext, "Fingerprint ist nicht definiert", false);
	}

	private void onNFCTagAllreadyAssigned() {
		UserInfo.dialog(mContext, "Fehler",
				"Der NFC Tag wurde schon zugewiesen!");
	}

	private void onNFCTagIsNullOrEmpty() {
		UserInfo.dialog(mContext, "Fehler",
				"Der NFC Tag konnte nicht verarbeitet werden!");
	}

	private void onQRCodeIsNullOrEmtpy() {
		UserInfo.dialog(mContext, "Fehler",
				"Der QR Code konnte nicht verarbeitet werden!");
	}

	private void onPoiDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "PoI existiert nicht!");
	}

	private void onQRCodeDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "Der QR Code ist ungültig!");
	}

	private void onNFCTagDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "Der NFC Tag ist ungültig!");
	}

	private void onPoiTypeIdDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "Der PoI Typ existiert nicht!");
	}

	private void onEndNodeNotFound() {
		UserInfo.dialog(mContext, "Fehler",
				"Der Zielpunkt konnte nicht gefunden werden!");
	}

	private void onStartNodeNotFound() {
		UserInfo.dialog(mContext, "Fehler",
				"Der Startpunkt konnte nicht gefunden werden!");
	}

	private void onNoRouteFound() {
		UserInfo.dialog(mContext, "Fehler",
				"Es konnte keine Route ermittelt werden");
	}

	private void onNodeIdDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "Der Knoten existiert nicht!");
	}

	private void onFloorIdDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "Die Ebene existiert nicht!");
	}

	private void onMapIdDoesNotExist() {
		UserInfo.dialog(mContext, "Fehler", "Die Map existiert nicht!");
	}

	private void onLoginInvalid() {
		UserInfo.toast(mContext, "Der Login-Versuch ist fehlgeschlagen", false);
	}

	private void onPasswordInvalid() {
		UserInfo.toast(mContext, "Das Passwort ist ungültig!", false);
	}

	private void onUserNameInvalid() {
		UserInfo.toast(mContext, "Der Benutzername ist ungültig", false);
	}

	private void onUserNameDuplicate() {
		UserInfo.toast(mContext, "Der Benutzername ist bereits vergeben!",
				false);
	}

	private void onUnknownError() {
		UserInfo.toast(mContext, "Unbekannter Fehler", false);
	}

	private void onDatabaseError() {
		UserInfo.dialog(mContext, "Entschuldigung",
				"Der Webservice arbeitet nicht einwandfrei!");
	}

	private void onNoError() {
		UserInfo.toast(mContext, "Es ist eigt. kein Fehler aufgetreten!", true);
	}

	private void onConnectionError() {
		UserInfo.dialog(mContext, "Entschuldigung",
				"Der Webserive ist nicht erreichbar!");
	}

}
