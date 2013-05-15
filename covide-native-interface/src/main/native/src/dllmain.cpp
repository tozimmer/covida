// dllmain.cpp : Definiert den Einstiegspunkt für die DLL-Anwendung.
#include "stdafx.h"

#pragma data_seg(".SHARED")
// shared data
HMODULE			g_this(0);
HHOOK			g_hook(0);
#pragma data_seg()
#pragma comment(linker, "/SECTION:.SHARED,RWS")
BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID /* lpReserved*/
					 )
{
	g_this = hModule;
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
	return TRUE;
}

