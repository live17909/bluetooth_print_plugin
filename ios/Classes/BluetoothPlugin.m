#import "BluetoothPlugin.h"
#import <bluetooth/bluetooth-Swift.h>

@implementation BluetoothPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBluetoothPlugin registerWithRegistrar:registrar];
}
@end
