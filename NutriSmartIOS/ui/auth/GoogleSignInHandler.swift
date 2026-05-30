//
//  GoogleSignInHandler.swift
//  NutriSmartIOS
//
//  Created by Alex on 30/05/2026.
//

import Foundation
import UIKit
import GoogleSignIn

@MainActor
class GoogleSignInHelper {
    static let shared = GoogleSignInHelper()
    
    func signIn(onSuccess: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        
        guard let presentingVC = UIApplication.shared.rootViewController else {
            onError("Root ViewController not found")
            return
        }
        
        GIDSignIn.sharedInstance.signOut()
        
        GIDSignIn.sharedInstance.signIn(withPresenting: presentingVC) { signInResult, error in
            
            if let error = error {
                let nsError = error as NSError
                if nsError.code == GIDSignInError.canceled.rawValue {
                    print("GoogleAuth: Login canceled.")
                } else {
                    onError("Error: \(error.localizedDescription)")
                }
                return
            }
            
            guard let user = signInResult?.user,
                  let idToken = user.idToken?.tokenString else {
                onError("Null Token.")
                return
            }
            
            onSuccess(idToken)
        }
    }
}

extension UIApplication {
    var rootViewController: UIViewController? {
        guard let windowScene = connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }) else {
            return nil
        }
        return window.rootViewController
    }
}
